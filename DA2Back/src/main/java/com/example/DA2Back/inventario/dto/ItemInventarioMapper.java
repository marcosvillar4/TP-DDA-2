package com.example.DA2Back.inventario.dto;

import com.example.DA2Back.inventario.dato.ItemInventario;

public class ItemInventarioMapper {

    public static ItemInventario toEntity(ItemInventarioCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        ItemInventario item = new ItemInventario();

        item.setCantidad(dto.getCantidad());

        return item;
    }

    public static ItemInventarioResponseDTO toResponseDTO(ItemInventario item) {

        if (item == null) {
            return null;
        }

        ItemInventarioResponseDTO dto = new ItemInventarioResponseDTO();

        dto.setId(item.getId());
        dto.setCantidad(item.getCantidad());

        if (item.getInventario() != null) {
            dto.setInventarioId(item.getInventario().getId());
        }

        if (item.getProducto() != null) {
            dto.setProductoId(item.getProducto().getId());
        }

        if (item.getDeposito() != null) {
            dto.setDepositoId(item.getDeposito().getId());
        }

        return dto;
    }
}