package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import java.util.List;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;

import lombok.Builder;
import lombok.Getter;
 
@Getter
@Builder
public class PerfilComercioAdminDTO implements PerfilAdminDTO {
 
    private final Long id;
    private final String nombreComercial;
    private final String razonSocial;
    private final String cuit;
    private final String direccion;
    private final String telefono;
    private final String email;
    private final int maxDepositos;
    private final List<DepositoResumen> depositos;
 
    /**
     * Resumen de cada depósito del comercio. "usuarioId" es el usuario que opera el
     * depósito: los depósitos nacen EN_EVALUACION, así que el admin los valida desde su detalle.
     */
    @Getter
    @Builder
    public static class DepositoResumen {
        private final Long id;
        private final String nombre;
        private final String direccion;
        private final Long usuarioId;
        private final String responsable;
        private final EstadoUsuario usuarioEstado;
    }
}
 
