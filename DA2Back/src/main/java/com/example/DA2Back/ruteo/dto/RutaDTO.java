package com.example.DA2Back.ruteo.dto;

import java.util.List;

import com.example.DA2Back.cotizacion.dto.PuntoDTO;

/**
 * Recorrido ordenado de un repartidor (o de un pedido).
 *
 * @param puntoPartida ubicacion actual del repartidor si la informo (puede ser null)
 * @param geometria    polilinea [lat, lng] lista para dibujar en un mapa
 * @param estimada     true si las distancias no vienen de un servicio de ruteo real
 * @param advertencias paradas que no se pudieron ubicar, etc.
 */
public record RutaDTO(
        PuntoDTO puntoPartida,
        List<ParadaRutaDTO> paradas,
        double distanciaTotalKm,
        int duracionTotalMin,
        List<double[]> geometria,
        String metodo,
        boolean estimada,
        List<String> advertencias) { }
