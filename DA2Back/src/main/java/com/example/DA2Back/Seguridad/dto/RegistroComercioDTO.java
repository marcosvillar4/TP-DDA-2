package com.example.DA2Back.Seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter 
public class RegistroComercioDTO extends RegisterDTO {

    @NotBlank
    private String nombreComercial;

    @NotBlank
    private String razonSocial;

    @NotBlank
    private String cuit;

    @NotBlank
    private String direccion;
}
