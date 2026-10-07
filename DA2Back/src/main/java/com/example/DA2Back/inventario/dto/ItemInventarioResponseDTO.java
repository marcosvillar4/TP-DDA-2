package com.example.DA2Back.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventarioResponseDTO {

    private Long id;

    private Long inventarioId;

    private Long productoId;

    private Long depositoId;

    private Integer cantidad;

    private Integer stockMinimo;

    // Datos descriptivos (solo lectura) para no obligar al cliente a cruzar
    // ids contra otros endpoints (algunos roles no tienen acceso a /productos).
    private String productoNombre;

    private String productoSku;

    private String depositoNombre;

    private Long comercioId;

    private String comercioNombre;
}
