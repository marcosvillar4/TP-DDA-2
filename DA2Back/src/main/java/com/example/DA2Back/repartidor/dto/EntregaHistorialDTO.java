package com.example.DA2Back.repartidor.dto;

import com.example.DA2Back.pedidos.dato.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaHistorialDTO {

    private String fecha;
    private Long pedidoId;
    private String direccionEntrega;
    private EstadoPedido resultado;
}
