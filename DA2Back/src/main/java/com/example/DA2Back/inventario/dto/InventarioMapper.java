package com.example.DA2Back.inventario.dto;

import java.util.stream.Collectors;

import com.example.DA2Back.inventario.dato.Inventario;

public class InventarioMapper {

    public static Inventario toEntity(InventarioCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Inventario inventario = new Inventario();

        return inventario;
    }

    public static InventarioResponseDTO toResponseDTO(Inventario inventario) {

        if (inventario == null) {
            return null;
        }

        InventarioResponseDTO dto = new InventarioResponseDTO();

        dto.setId(inventario.getId());

        if (inventario.getComercio() != null) {
            dto.setComercioId(inventario.getComercio().getId());
        }

        if (inventario.getItems() != null) {
            dto.setItemsIds(
                inventario.getItems()
                    .stream()
                    .map(item -> item.getId())
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
