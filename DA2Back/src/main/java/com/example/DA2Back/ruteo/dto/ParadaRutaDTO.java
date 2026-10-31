package com.example.DA2Back.ruteo.dto;

/**
 * Una parada del recorrido.
 *
 * @param tipo RETIRO (en el deposito/origen) o ENTREGA (en el destino)
 */
public record ParadaRutaDTO(
        int orden,
        String tipo,
        Long pedidoId,
        String direccion,
        double lat,
        double lng,
        double distanciaDesdeAnteriorKm,
        int duracionDesdeAnteriorMin,
        int duracionAcumuladaMin) { }
