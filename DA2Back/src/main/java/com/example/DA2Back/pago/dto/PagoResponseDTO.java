package com.example.DA2Back.pago.dto;

import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dato.MetodoPago;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago + datos del pedido al que corresponde (lo usan comercio y repartidor). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {

    private Long id;
    private BigDecimal monto;
    private EstadoPago estado;
    private MetodoPago metodoPago;
    private String referenciaOperacion;
    private String tarjetaUltimos4;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaPago;

    // Pedido asociado
    private Long pedidoId;
    private EstadoPedido pedidoEstado;
    private String direccionOrigen;
    private String direccionDestino;

    private Long comercioId;
    private String comercioNombre;

    private Long repartidorId;
    private String repartidorNombre;
}
