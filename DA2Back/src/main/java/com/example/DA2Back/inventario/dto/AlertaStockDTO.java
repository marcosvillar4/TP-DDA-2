package com.example.DA2Back.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockDTO {

    private Long itemInventarioId;

    private Long productoId;

    private String productoNombre;

    private String productoSku;

    private Long depositoId;

    private String depositoNombre;

    private Long comercioId;

    private String comercioNombre;

    private Integer cantidad;

    private Integer stockMinimo;
}
