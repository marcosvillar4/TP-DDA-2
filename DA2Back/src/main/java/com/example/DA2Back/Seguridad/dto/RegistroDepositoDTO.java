package com.example.DA2Back.Seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter 
public class RegistroDepositoDTO extends RegisterDTO {

    @NotBlank
    private String nombreDeposito;

    @NotBlank
    private String direccionDeposito;
}
