package com.example.DA2Back.deposito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoResponseDTO {

    private Long id;

    private String nombre;

    private String direccion;

    private Long comercioId;

    private Long usuarioId;

    private List<Long> itemsIds;
}
