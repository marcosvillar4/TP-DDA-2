package com.example.DA2Back.mapper;

import com.example.DA2Back.dto.usuario.UsuarioCreateDTO;
import com.example.DA2Back.dto.usuario.UsuarioResponseDTO;
import com.example.DA2Back.entites.Usuario;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();

        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword());
        usuario.setRol(dto.getRol());

        // El comercio se asigna desde el Service,
        // después de buscarlo mediante comercioId.

        return usuario;
    }

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {

        if (usuario == null) {
            return null;
        }

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());

        if (usuario.getComercio() != null) {
            dto.setComercioId(usuario.getComercio().getId());
        }

        return dto;
    }
}
