package com.example.DA2Back.deposito.dto;

import java.util.stream.Collectors;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.inventario.dato.ItemInventario;

public class DepositoMapper {

    public static Deposito toEntity(DepositoCreateDTO dto, Long usuarioId) {
        if (dto == null) {
            return null;
        }

        Deposito deposito = new Deposito();
        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setUsuarioId(usuarioId);

        return deposito;
    }

    public static DepositoResponseDTO toResponseDTO(Deposito deposito) {
        return toResponseDTO(deposito, null);
    }

    /**
     * Igual que {@link #toResponseDTO(Deposito)} pero completa los datos del
     * usuario responsable del depósito (puede ser null).
     */
    public static DepositoResponseDTO toResponseDTO(Deposito deposito, Usuario usuario) {
        if (deposito == null) {
            return null;
        }

        DepositoResponseDTO.DepositoResponseDTOBuilder builder = DepositoResponseDTO.builder()
                .id(deposito.getId())
                .nombre(deposito.getNombre())
                .direccion(deposito.getDireccion())
                .usuarioId(deposito.getUsuarioId()); // ya es Long, no navega a Usuario

        if (deposito.getComercio() != null) {
            builder.comercioId(deposito.getComercio().getId())
                   .comercioNombre(deposito.getComercio().getNombreComercial());
        }

        if (deposito.getItems() != null) {
            builder.itemsIds(
                    deposito.getItems().stream()
                            .map(ItemInventario::getId)
                            .collect(Collectors.toList())
            );
        }

        if (usuario != null) {
            builder.responsable(usuario.getNombre() + " " + usuario.getApellido())
                   .usuarioEmail(usuario.getEmail())
                   .usuarioEstado(usuario.getEstado());
        }

        return builder.build();
    }
}
