package com.example.DA2Back.pago.dto;

import com.example.DA2Back.pago.dato.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Datos para pagar un envio. Los campos de tarjeta aplican a TARJETA_*;
 * {@code cbu} aplica a TRANSFERENCIA. La validacion dependiente del metodo
 * se hace en ValidadorMedioPago.
 *
 * Sin @ToString a proposito: este objeto contiene datos de tarjeta y no debe
 * terminar en logs.
 */
@Getter
@Setter
@NoArgsConstructor
public class PagarPedidoDTO {

    @NotNull(message = "El metodo de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    private String numeroTarjeta;
    private String titular;
    /** MM/YY o MM/YYYY */
    private String vencimiento;
    private String cvv;

    /** 22 digitos, solo para TRANSFERENCIA. */
    private String cbu;
}
