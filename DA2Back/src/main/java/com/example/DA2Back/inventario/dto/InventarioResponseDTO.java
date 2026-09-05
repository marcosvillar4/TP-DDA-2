package com.example.DA2Back.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDTO {

    private Long id;

    private Long comercioId;

    private List<Long> itemsIds;
}
