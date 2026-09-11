package com.example.DA2Back.comercio.comercioDTOs;

import com.example.DA2Back.comercio.dato.Comercio;

public class ComercioMapper {

    public static Comercio toEntity(ComercioCreateDTO dto, Long usuarioId) {
        if (dto == null) {
            return null;
        }

        Comercio comercio = new Comercio();
        comercio.setNombreComercial(dto.getNombreComercial());
        comercio.setRazonSocial(dto.getRazonSocial());
        comercio.setDireccion(dto.getDireccion());
        comercio.setCUIT(dto.getCuit());
        comercio.setTelefono(dto.getTelefono());
        comercio.setEmail(dto.getEmail());
        comercio.setUsuarioId(usuarioId);

        return comercio;
    }

    public static ComercioResponseDTO toResponseDTO(Comercio comercio) {
        if (comercio == null) {
            return null;
        }

        return ComercioResponseDTO.builder()
                .id(comercio.getId())
                .nombreComercial(comercio.getNombreComercial())
                .razonSocial(comercio.getRazonSocial())
                .direccion(comercio.getDireccion())
                .cuit(comercio.getCUIT())
                .telefono(comercio.getTelefono())
                .email(comercio.getEmail())
                .usuarioId(comercio.getUsuarioId())
                .build();
    }
}
