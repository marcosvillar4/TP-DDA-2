package com.example.DA2Back.pago.negocio;

import com.example.DA2Back.pago.dato.MetodoPago;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pasarela simulada: no mueve dinero. Aprueba todo salvo estas tarjetas de prueba
 * (validas para Luhn), para poder probar el camino de rechazo desde la UI:
 *
 *   4242 4242 4242 4242  -> aprobada
 *   4000 0000 0000 9995  -> rechazada: fondos insuficientes
 *   4000 0000 0000 0002  -> rechazada: tarjeta rechazada por el emisor
 */
@Component
public class PasarelaPagoSimulada implements PasarelaPago {

    static final String TARJETA_SIN_FONDOS = "4000000000009995";
    static final String TARJETA_RECHAZADA = "4000000000000002";

    @Override
    public ResultadoPasarela procesar(MetodoPago metodo, String numeroTarjeta, String cbu, BigDecimal monto) {
        if (metodo != null && metodo.esTarjeta()) {
            if (TARJETA_SIN_FONDOS.equals(numeroTarjeta)) {
                return ResultadoPasarela.rechazado("Fondos insuficientes");
            }
            if (TARJETA_RECHAZADA.equals(numeroTarjeta)) {
                return ResultadoPasarela.rechazado("Tarjeta rechazada por el emisor");
            }
        }
        return ResultadoPasarela.aprobado("SIM-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase());
    }
}
