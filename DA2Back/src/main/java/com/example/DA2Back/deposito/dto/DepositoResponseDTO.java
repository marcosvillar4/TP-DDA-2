package com.example.DA2Back.deposito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoResponseDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private Long comercioId; // null si todavía no fue asociado a un comercio
    private Long usuarioId;
    private List<Long> itemsIds;

}
