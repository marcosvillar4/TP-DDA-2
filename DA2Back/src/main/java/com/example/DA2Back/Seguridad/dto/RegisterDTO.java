package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "rol")
@JsonSubTypes({
    @JsonSubTypes.Type(value = RegistroComercioDTO.class, name = "COMERCIO"),
    @JsonSubTypes.Type(value = RegistroDepositoDTO.class, name = "DEPOSITO")
    // REPARTIDOR pendiente
})
@Getter
@Setter
public abstract class RegisterDTO {

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String dni;

    @NotBlank
    private String telefono;

    private Rol rol;
}