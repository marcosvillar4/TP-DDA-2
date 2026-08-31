package com.example.DA2Back.comercio.negocio;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;

/**
 * Mapper estatico que convierte entre la entidad Comercio y sus DTOs.
 */
public class ComercioMapper {

    private ComercioMapper() {}

    public static Comercio toEntity(ComercioCreateDTO dto) {
        if (dto == null) return null;

        return Comercio.builder()
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .build();
    }

    public static ComercioResponseDTO toResponseDTO(Comercio comercio) {
        if (comercio == null) return null;

        return new ComercioResponseDTO(
                comercio.getId(),
                comercio.getNombre(),
                comercio.getDireccion(),
                comercio.getTelefono(),
                comercio.getEmail()
        );
    }
}
