package com.example.DA2Back.mapper;

import java.util.stream.Collectors;

import com.example.DA2Back.dto.comercio.ComercioCreateDTO;
import com.example.DA2Back.dto.comercio.ComercioResponseDTO;
import com.example.DA2Back.entites.Comercio;
import com.example.DA2Back.entites.Usuario;

public class ComercioMapper {

    public static Comercio toEntity(ComercioCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Comercio comercio = new Comercio();

        comercio.setNombre(dto.getNombre());
        comercio.setDireccion(dto.getDireccion());
        comercio.setTelefono(dto.getTelefono());
        comercio.setEmail(dto.getEmail());

        return comercio;
    }

    public static ComercioResponseDTO toResponseDTO(Comercio comercio) {

        if (comercio == null) {
            return null;
        }

        ComercioResponseDTO dto = new ComercioResponseDTO();

        dto.setId(comercio.getId());
        dto.setNombre(comercio.getNombre());
        dto.setDireccion(comercio.getDireccion());
        dto.setTelefono(comercio.getTelefono());
        dto.setEmail(comercio.getEmail());

        if (comercio.getUsuarios() != null) {
            dto.setUsuariosIds(
                comercio.getUsuarios()
                        .stream()
                        .map(Usuario::getId)
                        .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
