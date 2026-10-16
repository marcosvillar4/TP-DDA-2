package com.example.DA2Back.Seguridad.dto.perfilAdmin;

import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Getter;
 
/**
 * Detalle de usuario para administración: los datos del usuario + los de su rol.
 *
 * "usuario" va con @JsonUnwrapped, así el JSON sigue siendo plano y compatible con
 * lo que el front ya consume (id, nombre, email, rol, estado...), y solo suma "perfil".
 */
@Getter
@Builder
public class UsuarioAdminDetalleDTO {
 
    @JsonUnwrapped
    private final UsuarioResponseDTO usuario;
 
    /* null si el rol no tiene perfil (ADMIN) o si todavía no se creó. */
    private final PerfilAdminDTO perfil;
}