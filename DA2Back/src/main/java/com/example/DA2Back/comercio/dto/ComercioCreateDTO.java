package com.example.DA2Back.comercio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioCreateDTO {

    private String nombre;

    private String direccion;

    private String telefono;

    private String email;
}
