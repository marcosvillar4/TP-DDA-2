package com.example.DA2Back.Seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegistroRepartidorDTO extends RegisterDTO {

    @NotBlank
    private String vehiculo;
}
