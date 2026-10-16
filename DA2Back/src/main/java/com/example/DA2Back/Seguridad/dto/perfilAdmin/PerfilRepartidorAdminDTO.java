package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import com.example.DA2Back.repartidor.dato.EstadoRepartidor;

import lombok.Builder;
import lombok.Getter;
 
@Getter
@Builder
public class PerfilRepartidorAdminDTO implements PerfilAdminDTO {
 
    private final Long id;
    private final String vehiculo;
    private final String patente;
    private final String zona;
    private final EstadoRepartidor estado;
    private final boolean activo;
}
