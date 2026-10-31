package com.example.DA2Back.mapas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Fachada que usa el resto de la aplicacion para geocodificar direcciones y
 * calcular recorridos. Si el proveedor externo no responde (sin internet, limite
 * de uso, direccion desconocida) cae a un calculo local, de modo que crear un
 * pedido nunca falle por culpa de un servicio de mapas.
 */
@Service
public class ServicioMapas {

    private static final int MAX_CACHE = 2000;

    private final ProveedorMapas proveedor; // null => solo calculo local
    private final double factorRodeo;
    private final double velocidadKmh;
    private final Map<String, Coordenada> cache = new ConcurrentHashMap<>();

    public ServicioMapas(Optional<ProveedorMapas> proveedor,
                         @Value("${app.mapas.factor-rodeo:1.3}") double factorRodeo,
                         @Value("${app.mapas.velocidad-promedio-kmh:25}") double velocidadKmh) {
        this.proveedor = proveedor.orElse(null);
        this.factorRodeo = factorRodeo;
        this.velocidadKmh = velocidadKmh;
    }

    public String nombreProveedor() {
        return proveedor == null ? "LOCAL" : proveedor.nombre();
    }

    /** Devuelve la coordenada de una direccion, o vacio si no se pudo ubicar. */
    public Optional<Coordenada> geocodificar(String direccion) {
        if (direccion == null || direccion.isBlank() || proveedor == null) return Optional.empty();
        String normalizada = normalizar(direccion);
        Coordenada enCache = cache.get(normalizada.toLowerCase());
        if (enCache != null) return Optional.of(enCache);

        Optional<Coordenada> resultado = proveedor.geocodificar(normalizada);
        if (resultado.isEmpty() && !normalizada.equals(direccion.trim())) {
            resultado = proveedor.geocodificar(direccion.trim());
        }
        resultado.ifPresent(c -> {
            if (cache.size() >= MAX_CACHE) cache.clear();
            cache.put(normalizada.toLowerCase(), c);
        });
        return resultado;
    }

    /** Recorrido por los puntos en el orden dado (minimo 2 puntos). Nunca devuelve null. */
    public RutaCalculada calcularRuta(List<Coordenada> puntos) {
        if (puntos == null || puntos.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 puntos para calcular una ruta");
        }
        if (proveedor != null) {
            Optional<RutaCalculada> real = proveedor.calcularRuta(puntos);
            if (real.isPresent()) return real.get();
        }
        return estimarLinealmente(puntos);
    }

    /** Estimacion sin red: Haversine por tramo * factor de rodeo, velocidad promedio urbana. */
    RutaCalculada estimarLinealmente(List<Coordenada> puntos) {
        List<RutaCalculada.Tramo> tramos = new ArrayList<>();
        double totalKm = 0;
        double totalMin = 0;
        for (int i = 1; i < puntos.size(); i++) {
            double km = Coordenada.distanciaKm(puntos.get(i - 1), puntos.get(i)) * factorRodeo;
            double min = km / velocidadKmh * 60.0;
            tramos.add(new RutaCalculada.Tramo(km, min));
            totalKm += km;
            totalMin += min;
        }
        return new RutaCalculada(totalKm, totalMin, tramos, List.copyOf(puntos), "ESTIMACION_LINEAL", true);
    }

    /** Limpia espacios y expande abreviaturas que los geocodificadores no entienden (CABA). */
    static String normalizar(String direccion) {
        return direccion.trim().replaceAll("\\s+", " ")
                .replaceAll("(?i)\\bCABA\\b", "Buenos Aires");
    }
}
