package com.example.DA2Back.cotizacion.dto;

import java.math.BigDecimal;

/**
 * Cotizacion de un envio. Factores: distancia recorrida y cantidad de objetos.
 *
 * @param estimada true si la distancia NO salio de un servicio de ruteo real
 *                 (sin conexion, direccion no ubicada, etc.). El importe es aproximado.
 * @param metodo   OSRM, GOOGLE_ROUTES, ESTIMACION_LINEAL o DISTANCIA_POR_DEFECTO
 */
public record CotizacionDTO(
        double distanciaKm,
        int duracionEstimadaMin,
        int cantidadObjetos,
        BigDecimal costoBase,
        BigDecimal costoPorDistancia,
        BigDecimal costoPorObjetos,
        BigDecimal costoTotal,
        String moneda,
        boolean tarifaMinimaAplicada,
        boolean estimada,
        String metodo,
        PuntoDTO origen,
        PuntoDTO destino) { }
