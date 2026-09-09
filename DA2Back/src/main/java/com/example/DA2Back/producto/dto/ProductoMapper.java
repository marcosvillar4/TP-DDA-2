package com.example.DA2Back.producto.dto;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.producto.dato.Producto;

public class ProductoMapper {

    public static Producto toEntity(ProductoCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Producto producto = new Producto();

        producto.setSku(dto.getSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCategoria(dto.getCategoria());

        if (dto.getComercioId() != null) {
            Comercio comercio = new Comercio();
            comercio.setId(dto.getComercioId());
            producto.setComercio(comercio);
        }

        return producto;
    }

    public static Producto toEntity(ProductoUpdateDTO dto) {

        if (dto == null) {
            return null;
        }

        Producto producto = new Producto();

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCategoria(dto.getCategoria());

        return producto;
    }

    public static ProductoResponseDTO toResponseDTO(Producto producto) {

        if (producto == null) {
            return null;
        }

        ProductoResponseDTO dto = new ProductoResponseDTO();

        dto.setId(producto.getId());
        dto.setSku(producto.getSku());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCategoria(producto.getCategoria());
        dto.setEstado(producto.getEstado());

        if (producto.getComercio() != null) {
            dto.setComercioId(producto.getComercio().getId());
            dto.setComercioNombre(producto.getComercio().getNombre());
        }

        return dto;
    }
}
