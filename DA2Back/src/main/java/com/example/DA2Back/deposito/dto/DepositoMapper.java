package com.example.DA2Back.deposito.dto;

import java.util.stream.Collectors;

import com.example.DA2Back.deposito.dato.Deposito;

public class DepositoMapper {

    public static Deposito toEntity(DepositoCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        Deposito deposito = new Deposito();

        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());

        return deposito;
    }

    public static DepositoResponseDTO toResponseDTO(Deposito deposito) {

        if (deposito == null) {
            return null;
        }

        DepositoResponseDTO dto = new DepositoResponseDTO();

        dto.setId(deposito.getId());
        dto.setNombre(deposito.getNombre());
        dto.setDireccion(deposito.getDireccion());

        if (deposito.getComercio() != null) {
            dto.setComercioId(deposito.getComercio().getId());
        }

        if (deposito.getUsuario() != null) {
            dto.setUsuarioId(deposito.getUsuario().getId());
        }

        if (deposito.getItems() != null) {
            dto.setItemsIds(
                deposito.getItems()
                    .stream()
                    .map(item -> item.getId())
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
