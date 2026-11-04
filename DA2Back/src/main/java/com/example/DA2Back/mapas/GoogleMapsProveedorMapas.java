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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Proveedor opcional basado en Google Maps Platform. Se activa con
 * app.mapas.proveedor=google y app.mapas.google.api-key=...
 *
 * Usa Geocoding API y Routes API (computeRoutes). La Directions API clasica es
 * "Legacy" y no se puede habilitar en proyectos nuevos de Google Cloud.
 * Requiere una cuenta de facturacion (tiene cupo mensual gratis por SKU).
 */
@Component
@ConditionalOnProperty(name = "app.mapas.proveedor", havingValue = "google")
public class GoogleMapsProveedorMapas implements ProveedorMapas {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsProveedorMapas.class);
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    private final String apiKey;
    private final String pais;
    private final int timeoutMs;
    private final HttpClient http;

    public GoogleMapsProveedorMapas(
            @Value("${app.mapas.google.api-key:}") String apiKey,
            @Value("${app.mapas.pais:ar}") String pais,
            @Value("${app.mapas.timeout-ms:4000}") int timeoutMs) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.pais = pais;
        this.timeoutMs = timeoutMs;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        if (this.apiKey.isEmpty()) {
            log.warn("app.mapas.proveedor=google pero falta app.mapas.google.api-key: se usara el calculo local");
        }
    }

    @Override
    public String nombre() {
        return "GOOGLE_ROUTES";
    }

    @Override
    public Optional<Coordenada> geocodificar(String direccion) {
        if (apiKey.isEmpty()) return Optional.empty();
        try {
            String url = GEOCODE_URL + "?address=" + URLEncoder.encode(direccion, StandardCharsets.UTF_8)
                    + (pais == null || pais.isBlank() ? "" : "&region=" + pais)
                    + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs)).GET().build();
            Optional<String> cuerpo = enviar(req);
            if (cuerpo.isEmpty()) return Optional.empty();
            Map<String, Object> raiz = MiniJson.obj(MiniJson.parse(cuerpo.get()));
            if (raiz == null || !"OK".equals(raiz.get("status"))) {
                log.warn("Google Geocoding status={}", raiz == null ? null : raiz.get("status"));
                return Optional.empty();
            }
            List<Object> resultados = MiniJson.arr(raiz.get("results"));
            if (resultados == null || resultados.isEmpty()) return Optional.empty();
            Map<String, Object> geometry = MiniJson.obj(MiniJson.obj(resultados.get(0)).get("geometry"));
            Map<String, Object> loc = MiniJson.obj(geometry.get("location"));
            Coordenada c = new Coordenada(MiniJson.num(loc.get("lat")), MiniJson.num(loc.get("lng")));
            return c.esValida() ? Optional.of(c) : Optional.empty();
        } catch (Exception e) {
            log.warn("Google no pudo geocodificar '{}': {}", direccion, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public Optional<RutaCalculada> calcularRuta(List<Coordenada> puntos) {
        if (apiKey.isEmpty() || puntos.size() < 2) return Optional.empty();
        try {
            StringBuilder body = new StringBuilder("{\"origin\":").append(waypoint(puntos.get(0)))
                    .append(",\"destination\":").append(waypoint(puntos.get(puntos.size() - 1)));
            if (puntos.size() > 2) {
                body.append(",\"intermediates\":[");
                for (int i = 1; i < puntos.size() - 1; i++) {
                    if (i > 1) body.append(',');
                    body.append(waypoint(puntos.get(i)));
                }
                body.append(']');
            }
            body.append(",\"travelMode\":\"DRIVE\",\"routingPreference\":\"TRAFFIC_UNAWARE\"}");

            HttpRequest req = HttpRequest.newBuilder(URI.create(ROUTES_URL))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration,"
                            + "routes.legs.distanceMeters,routes.legs.duration,routes.polyline.encodedPolyline")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
            Optional<String> cuerpo = enviar(req);
            if (cuerpo.isEmpty()) return Optional.empty();

            Map<String, Object> raiz = MiniJson.obj(MiniJson.parse(cuerpo.get()));
            List<Object> rutas = raiz == null ? null : MiniJson.arr(raiz.get("routes"));
            if (rutas == null || rutas.isEmpty()) return Optional.empty();
            Map<String, Object> ruta = MiniJson.obj(rutas.get(0));

            double km = metros(ruta.get("distanceMeters")) / 1000.0;
            double min = segundos(ruta.get("duration")) / 60.0;
            List<RutaCalculada.Tramo> tramos = new ArrayList<>();
            for (Object l : MiniJson.arr(ruta.get("legs"))) {
                Map<String, Object> leg = MiniJson.obj(l);
                tramos.add(new RutaCalculada.Tramo(metros(leg.get("distanceMeters")) / 1000.0,
                        segundos(leg.get("duration")) / 60.0));
            }
            if (tramos.size() != puntos.size() - 1) return Optional.empty();

            Map<String, Object> poly = MiniJson.obj(ruta.get("polyline"));
            List<Coordenada> geometria = poly == null ? List.of()
                    : decodificarPolilinea((String) poly.get("encodedPolyline"));
            return Optional.of(new RutaCalculada(km, min, tramos, geometria, nombre(), false));
        } catch (Exception e) {
            log.warn("Google Routes no pudo calcular la ruta: {}", e.toString());
            return Optional.empty();
        }
    }

    private Optional<String> enviar(HttpRequest req) throws Exception {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) {
                log.warn("Google respondio {}: {}", resp.statusCode(), resp.body());
                return Optional.empty();
            }
            return Optional.of(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private static String waypoint(Coordenada c) {
        return String.format(Locale.ROOT,
                "{\"location\":{\"latLng\":{\"latitude\":%.6f,\"longitude\":%.6f}}}", c.lat(), c.lng());
    }

    private static double metros(Object o) {
        Double d = MiniJson.num(o);
        return d == null ? 0 : d; // Google omite el campo cuando vale 0
    }

    /** Google devuelve la duracion como "1234s". */
    private static double segundos(Object o) {
        if (!(o instanceof String s) || s.isEmpty()) return 0;
        return Double.parseDouble(s.endsWith("s") ? s.substring(0, s.length() - 1) : s);
    }

    /** Decodifica el formato "Encoded Polyline" de Google (precision 5). */
    static List<Coordenada> decodificarPolilinea(String codigo) {
        List<Coordenada> puntos = new ArrayList<>();
        if (codigo == null) return puntos;
        int i = 0;
        int lat = 0;
        int lng = 0;
        while (i < codigo.length()) {
            int[] r = new int[2];
            for (int k = 0; k < 2; k++) {
                int shift = 0;
                int resultado = 0;
                int b;
                do {
                    b = codigo.charAt(i++) - 63;
                    resultado |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                r[k] = (resultado & 1) != 0 ? ~(resultado >> 1) : (resultado >> 1);
            }
            lat += r[0];
            lng += r[1];
            puntos.add(new Coordenada(lat / 1e5, lng / 1e5));
        }
        return puntos;
    }
}
