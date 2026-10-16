package com.example.DA2Back.repartidor.dto;

import java.time.LocalDateTime;

import com.example.DA2Back.pedidos.dato.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoActualDTO {

    private Long id;
    private EstadoPedido estado;
    private String direccionDestino;
    private String direccionOrigen;
    private LocalDateTime fechaCreacion;
}
