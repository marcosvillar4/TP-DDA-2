package com.example.DA2Back.comercio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida para el recurso Comercio.
 * No expone la entidad JPA directamente; desacopla la capa web del dominio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioResponseDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
}