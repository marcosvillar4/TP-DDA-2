package com.example.DA2Back.pedidos.dto;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO de salida que representa un Pedido en la respuesta HTTP.
 * Garantiza que la entidad JPA nunca sea serializada directamente en la capa web.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private Long comercioId;
    private String direccionDestino;
    private EstadoPedido estado;
}