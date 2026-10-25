package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import lombok.Builder;
import lombok.Getter;
 
@Getter
@Builder
public class PerfilDepositoAdminDTO implements PerfilAdminDTO {
 
    private final Long id;
    private final String nombre;
    private final String direccion;
    /* null si el depósito todavía no fue asociado a un comercio. */
    private final ComercioAsociado comercio;
 
    /* usuarioId permite al front navegar al detalle del usuario dueño del comercio. */
    @Getter
    @Builder
    public static class ComercioAsociado {
        private final Long id;
        private final String nombreComercial;
        private final Long usuarioId;
    }
}
