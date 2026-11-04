package com.example.DA2Back.mapas;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Proveedor GRATUITO y sin API key basado en OpenStreetMap:
 *  - Geocodificacion: Nominatim (max. 1 request/segundo, requiere User-Agent propio).
 *  - Ruteo: servidor demo de OSRM (distancia/tiempo por calles + geometria del recorrido).
 *
 * Es el proveedor por defecto. Para produccion real conviene alojar Nominatim/OSRM propios.
 */
@Component
@ConditionalOnProperty(name = "app.mapas.proveedor", havingValue = "osm", matchIfMissing = true)
public class OsmProveedorMapas implements ProveedorMapas {

    private static final Logger log = LoggerFactory.getLogger(OsmProveedorMapas.class);
    private static final long INTERVALO_NOMINATIM_MS = 1100;
    private static final int MAX_PUNTOS_GEOMETRIA = 1500;

    private final String nominatimUrl;
    private final String osrmUrl;
    private final String pais;
    private final String userAgent;
    private final int timeoutMs;
    private final HttpClient http;
    private long ultimoRequestNominatim = 0;

    public OsmProveedorMapas(
            @Value("${app.mapas.osm.nominatim-url:https://nominatim.openstreetmap.org}") String nominatimUrl,
            @Value("${app.mapas.osm.osrm-url:https://router.project-osrm.org}") String osrmUrl,
            @Value("${app.mapas.pais:ar}") String pais,
            @Value("${app.mapas.user-agent:LogiRed-TP-DA2/1.0 (proyecto academico)}") String userAgent,
            @Value("${app.mapas.timeout-ms:4000}") int timeoutMs) {
        this.nominatimUrl = quitarBarraFinal(nominatimUrl);
        this.osrmUrl = quitarBarraFinal(osrmUrl);
        this.pais = pais;
        this.userAgent = userAgent;
        this.timeoutMs = timeoutMs;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
    }

    @Override
    public String nombre() {
        return "OSRM";
    }

    @Override
    public Optional<Coordenada> geocodificar(String direccion) {
        try {
            esperarTurnoNominatim();
            String url = nominatimUrl + "/search?format=jsonv2&limit=1"
                    + (pais == null || pais.isBlank() ? "" : "&countrycodes=" + pais)
                    + "&q=" + URLEncoder.encode(direccion, StandardCharsets.UTF_8);
            Optional<String> cuerpo = get(url);
            if (cuerpo.isEmpty()) return Optional.empty();
            List<Object> resultados = MiniJson.arr(MiniJson.parse(cuerpo.get()));
            if (resultados == null || resultados.isEmpty()) return Optional.empty();
            Map<String, Object> primero = MiniJson.obj(resultados.get(0));
            Double lat = MiniJson.num(primero.get("lat"));
            Double lon = MiniJson.num(primero.get("lon"));
            if (lat == null || lon == null) return Optional.empty();
            Coordenada c = new Coordenada(lat, lon);
            return c.esValida() ? Optional.of(c) : Optional.empty();
        } catch (Exception e) {
            log.warn("Nominatim no pudo geocodificar '{}': {}", direccion, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public Optional<RutaCalculada> calcularRuta(List<Coordenada> puntos) {
        try {
            StringBuilder coords = new StringBuilder();
            for (Coordenada p : puntos) {
                if (coords.length() > 0) coords.append(';');
                coords.append(p.lng()).append(',').append(p.lat());
            }
            String url = osrmUrl + "/route/v1/driving/" + coords
                    + "?overview=full&geometries=geojson&steps=false";
            Optional<String> cuerpo = get(url);
            if (cuerpo.isEmpty()) return Optional.empty();
            Map<String, Object> raiz = MiniJson.obj(MiniJson.parse(cuerpo.get()));
            if (raiz == null || !"Ok".equals(raiz.get("code"))) return Optional.empty();
            List<Object> rutas = MiniJson.arr(raiz.get("routes"));
            if (rutas == null || rutas.isEmpty()) return Optional.empty();
            Map<String, Object> ruta = MiniJson.obj(rutas.get(0));

            double km = MiniJson.num(ruta.get("distance")) / 1000.0;
            double min = MiniJson.num(ruta.get("duration")) / 60.0;

            List<RutaCalculada.Tramo> tramos = new ArrayList<>();
            List<Object> legs = MiniJson.arr(ruta.get("legs"));
            if (legs != null) {
                for (Object l : legs) {
                    Map<String, Object> leg = MiniJson.obj(l);
                    tramos.add(new RutaCalculada.Tramo(
                            MiniJson.num(leg.get("distance")) / 1000.0,
                            MiniJson.num(leg.get("duration")) / 60.0));
                }
            }
            if (tramos.size() != puntos.size() - 1) return Optional.empty();

            List<Coordenada> geometria = new ArrayList<>();
            Map<String, Object> geom = MiniJson.obj(ruta.get("geometry"));
            List<Object> pares = geom == null ? null : MiniJson.arr(geom.get("coordinates"));
            if (pares != null) {
                int salto = Math.max(1, pares.size() / MAX_PUNTOS_GEOMETRIA);
                for (int i = 0; i < pares.size(); i += salto) {
                    List<Object> par = MiniJson.arr(pares.get(i));
                    geometria.add(new Coordenada(MiniJson.num(par.get(1)), MiniJson.num(par.get(0))));
                }
                List<Object> ultimo = MiniJson.arr(pares.get(pares.size() - 1));
                Coordenada fin = new Coordenada(MiniJson.num(ultimo.get(1)), MiniJson.num(ultimo.get(0)));
                if (!geometria.get(geometria.size() - 1).equals(fin)) geometria.add(fin);
            }
            return Optional.of(new RutaCalculada(km, min, tramos, geometria, nombre(), false));
        } catch (Exception e) {
            log.warn("OSRM no pudo calcular la ruta: {}", e.toString());
            return Optional.empty();
        }
    }

    private Optional<String> get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .GET().build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) {
                log.warn("Respuesta {} de {}", resp.statusCode(), url);
                return Optional.empty();
            }
            return Optional.of(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /** Politica de uso de Nominatim: como maximo un request por segundo. */
    private synchronized void esperarTurnoNominatim() throws InterruptedException {
        long espera = ultimoRequestNominatim + INTERVALO_NOMINATIM_MS - System.currentTimeMillis();
        if (espera > 0) Thread.sleep(espera);
        ultimoRequestNominatim = System.currentTimeMillis();
    }

    private static String quitarBarraFinal(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
