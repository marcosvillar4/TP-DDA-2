package com.example.DA2Back.mapas;

import java.util.List;
import java.util.Optional;

/**
 * Puerto hacia un servicio externo de mapas. Hay una implementacion gratuita
 * (OpenStreetMap: Nominatim + OSRM) y una opcional de Google Maps.
 * Las implementaciones NO deben lanzar excepciones: ante cualquier problema
 * devuelven Optional.empty() y {@link ServicioMapas} aplica un calculo local.
 */
public interface ProveedorMapas {

    String nombre();

    Optional<Coordenada> geocodificar(String direccion);

    /** Recorrido por carretera pasando por los puntos en el orden dado (minimo 2). */
    Optional<RutaCalculada> calcularRuta(List<Coordenada> puntos);
}
