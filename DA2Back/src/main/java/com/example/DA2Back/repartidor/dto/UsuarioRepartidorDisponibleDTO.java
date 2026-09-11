package com.example.DA2Back.repartidor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRepartidorDisponibleDTO {

    private Long id;
    private String username;
    private String email;
}
