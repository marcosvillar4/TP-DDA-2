package com.example.DA2Back.mapas;

import java.util.List;

/**
 * Resultado de calcular un recorrido que pasa por varios puntos en orden.
 *
 * @param distanciaKm distancia total
 * @param duracionMin duracion total estimada
 * @param tramos      un tramo por cada par de puntos consecutivos
 * @param geometria   polilinea para dibujar en un mapa (puede estar vacia)
 * @param metodo      como se obtuvo (OSRM, GOOGLE_ROUTES, ESTIMACION_LINEAL)
 * @param estimada    true si NO viene de un servicio de ruteo real
 */
public record RutaCalculada(double distanciaKm, double duracionMin, List<Tramo> tramos,
                            List<Coordenada> geometria, String metodo, boolean estimada) {

    public record Tramo(double distanciaKm, double duracionMin) { }
}
