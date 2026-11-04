package com.example.DA2Back.cotizacion.negocio;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Regla de precio (configurable en application.properties):
 *
 *   total = max( tarifaMinima,  tarifaBase + km * precioPorKm + objetos * precioPorObjeto )
 */
@Component
public class CalculadoraTarifa {

    /** Desglose del calculo. */
    public record Desglose(BigDecimal base, BigDecimal porDistancia, BigDecimal porObjetos,
                           BigDecimal total, boolean minimaAplicada, String moneda) { }

    private final BigDecimal tarifaBase;
    private final BigDecimal precioPorKm;
    private final BigDecimal precioPorObjeto;
    private final BigDecimal tarifaMinima;
    private final String moneda;

    public CalculadoraTarifa(
            @Value("${app.cotizacion.tarifa-base:1500}") BigDecimal tarifaBase,
            @Value("${app.cotizacion.precio-por-km:450}") BigDecimal precioPorKm,
            @Value("${app.cotizacion.precio-por-objeto:120}") BigDecimal precioPorObjeto,
            @Value("${app.cotizacion.tarifa-minima:2000}") BigDecimal tarifaMinima,
            @Value("${app.cotizacion.moneda:ARS}") String moneda) {
        this.tarifaBase = tarifaBase;
        this.precioPorKm = precioPorKm;
        this.precioPorObjeto = precioPorObjeto;
        this.tarifaMinima = tarifaMinima;
        this.moneda = moneda;
    }

    public Desglose calcular(double distanciaKm, int cantidadObjetos) {
        if (Double.isNaN(distanciaKm) || distanciaKm < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa");
        }
        if (cantidadObjetos < 0) {
            throw new IllegalArgumentException("La cantidad de objetos no puede ser negativa");
        }
        BigDecimal base = dinero(tarifaBase);
        BigDecimal porDistancia = dinero(BigDecimal.valueOf(distanciaKm).multiply(precioPorKm));
        BigDecimal porObjetos = dinero(BigDecimal.valueOf(cantidadObjetos).multiply(precioPorObjeto));
        BigDecimal subtotal = base.add(porDistancia).add(porObjetos);
        boolean minima = subtotal.compareTo(tarifaMinima) < 0;
        BigDecimal total = minima ? dinero(tarifaMinima) : subtotal;
        return new Desglose(base, porDistancia, porObjetos, total, minima, moneda);
    }

    private static BigDecimal dinero(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
