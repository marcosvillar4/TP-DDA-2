package com.example.DA2Back.comercio.comercioDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioResponseDTO {

    private Long id;
    private String nombreComercial;
    private String razonSocial;
    private String direccion;
    private String cuit;
    private String telefono;
    private String email;
    private Long usuarioId;
}