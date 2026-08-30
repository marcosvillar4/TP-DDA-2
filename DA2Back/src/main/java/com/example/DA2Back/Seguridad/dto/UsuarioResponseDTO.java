package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.Rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;

    private String email;

    private String username;

    private Rol rol;

    private boolean activo;

    // Getters y Setters
}
