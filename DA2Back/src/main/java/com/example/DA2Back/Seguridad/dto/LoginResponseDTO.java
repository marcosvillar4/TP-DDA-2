package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;

    private Long id;

    private String username;

    private Rol rol;
}
