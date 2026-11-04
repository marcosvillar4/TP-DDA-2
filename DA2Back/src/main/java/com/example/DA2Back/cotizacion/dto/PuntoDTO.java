package com.example.DA2Back.cotizacion.dto;

/** Direccion con su ubicacion (lat/lng pueden ser null si no se pudo geocodificar). */
public record PuntoDTO(String direccion, Double lat, Double lng) { }
