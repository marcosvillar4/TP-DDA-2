package com.example.DA2Back.Seguridad.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Datos para dar de alta un depósito junto con su usuario responsable (rol DEPOSITO).
 *
 * Ya NO es un tipo de auto-registro: no figura en los subtipos de
 * {@link RegisterDTO}, por lo que POST /auth/register rechaza el rol DEPOSITO.
 * Solo se usa en POST /depositos, invocado por un COMERCIO autenticado, y el
 * depósito queda vinculado a ese comercio.
 *
 * Reutiliza los campos comunes de {@link RegisterDTO} (email, password, nombre,
 * apellido, dni, telefono). @JsonTypeInfo(NONE) evita que Jackson exija el
 * discriminador "rol" heredado de la clase base; el rol lo fija el servidor.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@Getter 
public class RegistroDepositoDTO extends RegisterDTO {

    @NotBlank
    private String nombreDeposito;

    @NotBlank
    private String direccionDeposito;
}
