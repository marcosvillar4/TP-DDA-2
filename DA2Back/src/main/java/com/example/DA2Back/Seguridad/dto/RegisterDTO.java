package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    private String username;

    private String password;

    private String email;

    private Rol rol;


    // Getters y Setters
}