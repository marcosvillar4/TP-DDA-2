package com.example.DA2Back.dto;

import com.example.DA2Back.model.EstadoPedido;
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