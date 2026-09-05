package com.example.DA2Back.deposito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoCreateDTO {

    private String nombre;

    private String direccion;

    private Long comercioId;
}
