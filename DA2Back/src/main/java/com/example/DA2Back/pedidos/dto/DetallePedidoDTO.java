package com.example.DA2Back.pedidos.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetallePedidoDTO {
    private Long productoId;
    private Integer cantidad;
}
