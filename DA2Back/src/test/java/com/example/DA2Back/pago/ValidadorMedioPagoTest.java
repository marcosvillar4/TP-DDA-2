package com.example.DA2Back.pago;

import com.example.DA2Back.pago.negocio.ValidadorMedioPago;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorMedioPagoTest {

    private static final YearMonth HOY = YearMonth.of(2026, 11);
    private static final String VISA_OK = "4242424242424242";
    // CBU con digitos verificadores correctos (generado con el algoritmo del BCRA).
    private static final String CBU_OK = "0170099220000000123453";

    private String tarjeta(String numero, String titular, String venc, String cvv) {
        return ValidadorMedioPago.validarTarjeta(numero, titular, venc, cvv, HOY);
    }

    @Test
    void tarjetaValidaSeNormalizaADigitos() {
        assertEquals(VISA_OK, tarjeta("4242 4242-4242 4242", "Juan Perez", "12/28", "123"));
    }

    @Test
    void tarjetaQueVenceEsteMesEsValida() {
        assertEquals(VISA_OK, tarjeta(VISA_OK, "Juan Perez", "11/26", "123"));
    }

    @Test
    void tarjetaVencidaSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "Juan Perez", "10/26", "123"));
    }

    @Test
    void vencimientoConFormatoInvalidoOLejanoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "Juan Perez", "13/28", "123"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "Juan Perez", "1228", "123"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "Juan Perez", "01/60", "123"));
    }

    @Test
    void numeroInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> tarjeta("4242424242424241", "Juan Perez", "12/28", "123")); // Luhn
        assertThrows(IllegalArgumentException.class, () -> tarjeta("4242abcd42424242", "Juan Perez", "12/28", "123"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta("4242", "Juan Perez", "12/28", "123"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta(null, "Juan Perez", "12/28", "123"));
    }

    @Test
    void cvvYTitularInvalidosSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "Juan Perez", "12/28", "12"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "Juan Perez", "12/28", "12a"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, "J1", "12/28", "123"));
        assertThrows(IllegalArgumentException.class, () -> tarjeta(VISA_OK, null, "12/28", "123"));
    }

    @Test
    void cbuValidoSePasaTalCual() {
        assertEquals(CBU_OK, ValidadorMedioPago.validarCbu(CBU_OK));
    }

    @Test
    void cbuInvalidoSeRechaza() {
        String dvIncorrecto = CBU_OK.substring(0, 21) + "4";
        assertThrows(IllegalArgumentException.class, () -> ValidadorMedioPago.validarCbu(dvIncorrecto));
        assertThrows(IllegalArgumentException.class, () -> ValidadorMedioPago.validarCbu("123"));
        assertThrows(IllegalArgumentException.class, () -> ValidadorMedioPago.validarCbu("01700992200000001234ab"));
        assertThrows(IllegalArgumentException.class, () -> ValidadorMedioPago.validarCbu(null));
    }
}
