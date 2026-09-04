package com.example.DA2Back.producto.dto;

import com.example.DA2Back.producto.dato.Producto;

public class ProductoMapper {

    public static Producto toEntity(ProductoCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Producto producto = new Producto();

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());

        return producto;
    }

    public static ProductoResponseDTO toResponseDTO(Producto producto) {

        if (producto == null) {
            return null;
        }

        ProductoResponseDTO dto = new ProductoResponseDTO();

        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());

        return dto;
    }
}
