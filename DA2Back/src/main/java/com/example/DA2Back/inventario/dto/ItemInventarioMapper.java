package com.example.DA2Back.inventario.dto;

import com.example.DA2Back.inventario.dato.ItemInventario;

public class ItemInventarioMapper {

    public static ItemInventario toEntity(ItemInventarioCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        ItemInventario item = new ItemInventario();

        item.setCantidad(dto.getCantidad());
        item.setStockMinimo(dto.getStockMinimo());

        return item;
    }

    public static ItemInventarioResponseDTO toResponseDTO(ItemInventario item) {

        if (item == null) {
            return null;
        }

        ItemInventarioResponseDTO dto = new ItemInventarioResponseDTO();

        dto.setId(item.getId());
        dto.setCantidad(item.getCantidad());
        dto.setStockMinimo(item.getStockMinimo() != null ? item.getStockMinimo() : 5);

        if (item.getInventario() != null) {
            dto.setInventarioId(item.getInventario().getId());
        }

        if (item.getProducto() != null) {
            dto.setProductoId(item.getProducto().getId());
            dto.setProductoNombre(item.getProducto().getNombre());
            dto.setProductoSku(item.getProducto().getSku());
        }

        if (item.getDeposito() != null) {
            dto.setDepositoId(item.getDeposito().getId());
            dto.setDepositoNombre(item.getDeposito().getNombre());
        }

        if (item.getInventario() != null && item.getInventario().getComercio() != null) {
            dto.setComercioId(item.getInventario().getComercio().getId());
            dto.setComercioNombre(item.getInventario().getComercio().getNombreComercial());
        }

        return dto;
    }
}
