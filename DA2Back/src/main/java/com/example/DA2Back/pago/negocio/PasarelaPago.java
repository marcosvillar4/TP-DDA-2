package com.example.DA2Back.pago.negocio;

import com.example.DA2Back.pago.dato.MetodoPago;

import java.math.BigDecimal;

/**
 * Puerto hacia el procesador de pagos. Hoy lo implementa PasarelaPagoSimulada;
 * para integrar un proveedor real alcanza con otra implementacion de esta interfaz.
 */
public interface PasarelaPago {

    /**
     * @param numeroTarjeta digitos de la tarjeta (null si el metodo no es tarjeta)
     * @param cbu           CBU de origen (null si el metodo no es transferencia)
     */
    ResultadoPasarela procesar(MetodoPago metodo, String numeroTarjeta, String cbu, BigDecimal monto);

    record ResultadoPasarela(boolean aprobado, String referencia, String motivoRechazo) {

        public static ResultadoPasarela aprobado(String referencia) {
            return new ResultadoPasarela(true, referencia, null);
        }

        public static ResultadoPasarela rechazado(String motivo) {
            return new ResultadoPasarela(false, null, motivo);
        }
    }
}
