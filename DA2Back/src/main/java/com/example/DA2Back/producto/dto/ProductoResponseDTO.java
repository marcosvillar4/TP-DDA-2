package com.example.DA2Back.producto.dto;

import com.example.DA2Back.producto.dato.EstadoProducto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Long id;

    private String sku;

    private String nombre;

    private String descripcion;

    private String categoria;

    private EstadoProducto estado;

    private Long comercioId;

    private String comercioNombre;
}
