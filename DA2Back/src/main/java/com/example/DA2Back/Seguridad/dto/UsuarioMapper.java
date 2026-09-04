package com.example.DA2Back.Seguridad.dto;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .username(usuario.getNombreUsuario())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .build();
    }

    public Usuario toEntity(RegisterDTO dto) {
        if (dto == null) {
            return null;
        }

        return Usuario.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .rol(dto.getRol())
                .build();
    }
}