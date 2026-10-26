package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    /** Access token JWT (vida corta). */
    private String token;
    /** Refresh token opaco para renovar el access token (SEC-W03). */
    private String refreshToken;
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Rol rol;
}
