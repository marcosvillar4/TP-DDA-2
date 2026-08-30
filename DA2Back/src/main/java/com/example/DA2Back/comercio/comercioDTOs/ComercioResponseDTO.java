package com.example.DA2Back.comercio.comercioDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioResponseDTO {

    private Long id;

    private String nombre;

    private String direccion;

    private String telefono;

    private String email;

    private List<Long> usuariosIds;
}