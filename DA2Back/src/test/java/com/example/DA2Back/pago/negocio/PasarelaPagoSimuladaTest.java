package com.example.DA2Back.pago.negocio;

import com.example.DA2Back.pago.dato.MetodoPago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasarelaPagoSimuladaTest {

    private final PasarelaPagoSimulada pasarela = new PasarelaPagoSimulada();
    private static final BigDecimal MONTO = new BigDecimal("100");

    @Test
    @DisplayName("Tarjeta normal: aprobada con referencia SIM-")
    void tarjetaNormal_aprobada() {
        PasarelaPago.ResultadoPasarela r = pasarela.procesar(MetodoPago.TARJETA_CREDITO, "4242424242424242", null, MONTO);
        assertTrue(r.aprobado());
        assertTrue(r.referencia().startsWith("SIM-"));
        assertNull(r.motivoRechazo());
    }

    @Test
    @DisplayName("Tarjeta de prueba sin fondos: rechazada")
    void sinFondos_rechazada() {
        PasarelaPago.ResultadoPasarela r = pasarela.procesar(MetodoPago.TARJETA_DEBITO,
                PasarelaPagoSimulada.TARJETA_SIN_FONDOS, null, MONTO);
        assertFalse(r.aprobado());
        assertEquals("Fondos insuficientes", r.motivoRechazo());
        assertNull(r.referencia());
    }

    @Test
    @DisplayName("Tarjeta de prueba rechazada por el emisor")
    void rechazadaPorEmisor() {
        PasarelaPago.ResultadoPasarela r = pasarela.procesar(MetodoPago.TARJETA_CREDITO,
                PasarelaPagoSimulada.TARJETA_RECHAZADA, null, MONTO);
        assertFalse(r.aprobado());
        assertEquals("Tarjeta rechazada por el emisor", r.motivoRechazo());
    }

    @Test
    @DisplayName("Transferencia y metodo nulo se aprueban aunque el numero coincida con una tarjeta de prueba")
    void transferenciaYNulo_aprobados() {
        assertTrue(pasarela.procesar(MetodoPago.TRANSFERENCIA, PasarelaPagoSimulada.TARJETA_SIN_FONDOS, "0170099220000000123453", MONTO).aprobado());
        assertTrue(pasarela.procesar(null, null, null, MONTO).aprobado());
    }
}
