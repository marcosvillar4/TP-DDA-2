package com.example.DA2Back.dto.usuario;

import com.example.DA2Back.entites.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {

    private String username;

    private String password;

    private Rol rol;

    private Long comercioId;

    // Getters y Setters
}