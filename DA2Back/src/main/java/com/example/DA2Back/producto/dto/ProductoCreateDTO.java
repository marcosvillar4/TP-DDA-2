package com.example.DA2Back.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCreateDTO {

    private String nombre;

    private String descripcion;

    private Double precio;
}
