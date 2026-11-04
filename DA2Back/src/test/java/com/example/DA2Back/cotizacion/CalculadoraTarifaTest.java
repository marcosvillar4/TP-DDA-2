package com.example.DA2Back.cotizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.DA2Back.cotizacion.negocio.CalculadoraTarifa;

class CalculadoraTarifaTest {

    // base 1500 + 450/km + 120/objeto, minimo 2000
    private final CalculadoraTarifa tarifa = new CalculadoraTarifa(
            new BigDecimal("1500"), new BigDecimal("450"), new BigDecimal("120"),
            new BigDecimal("2000"), "ARS");

    @Test
    void sumaBaseDistanciaYObjetos() {
        var d = tarifa.calcular(5.0, 3);
        assertEquals(new BigDecimal("1500.00"), d.base());
        assertEquals(new BigDecimal("2250.00"), d.porDistancia());
        assertEquals(new BigDecimal("360.00"), d.porObjetos());
        assertEquals(new BigDecimal("4110.00"), d.total());
        assertFalse(d.minimaAplicada());
        assertEquals("ARS", d.moneda());
    }

    @Test
    void aplicaLaTarifaMinima() {
        var d = tarifa.calcular(0.1, 0); // 1500 + 45 = 1545 < 2000
        assertEquals(new BigDecimal("2000.00"), d.total());
        assertTrue(d.minimaAplicada());
    }

    @Test
    void masDistanciaOMasObjetosNuncaBajanElPrecio() {
        BigDecimal a = tarifa.calcular(8, 2).total();
        assertTrue(tarifa.calcular(9, 2).total().compareTo(a) > 0);
        assertTrue(tarifa.calcular(8, 3).total().compareTo(a) > 0);
    }

    @Test
    void redondeaADosDecimales() {
        var d = tarifa.calcular(3.333, 1); // 3.333 * 450 = 1499.85
        assertEquals(new BigDecimal("1499.85"), d.porDistancia());
        assertEquals(2, d.total().scale());
    }

    @Test
    void rechazaValoresNegativos() {
        assertThrows(IllegalArgumentException.class, () -> tarifa.calcular(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> tarifa.calcular(1, -1));
        assertThrows(IllegalArgumentException.class, () -> tarifa.calcular(Double.NaN, 1));
    }
}
