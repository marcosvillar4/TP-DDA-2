package com.example.DA2Back.comercio.comercioDTOs;

import com.example.DA2Back.comercio.dato.Comercio;

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


        return dto;
    }
}
