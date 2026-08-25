package com.example.DA2Back.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para la creacion de un nuevo Pedido.
 * Desacopla la capa web de la entidad de dominio.
 */
@Getter
@Setter
@NoArgsConstructor
public class CrearPedidoDTO {

    private Long comercioId;
    private String direccionDestino;
}