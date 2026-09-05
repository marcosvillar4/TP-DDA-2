package com.example.DA2Back.pedidos.dto;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para actualizar el estado de un Pedido existente.
 */
@Getter
@Setter
@NoArgsConstructor
public class ActualizarEstadoDTO {

    private EstadoPedido estado;
}