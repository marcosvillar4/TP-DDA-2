package com.example.DA2Back.Seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPasswordAdminDTO {
    @NotBlank @Size(min = 8)
    private String passwordNuevo;
}