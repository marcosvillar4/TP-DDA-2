package com.example.DA2Back.deposito.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class AsociarDepositoDTO {
    @NotNull
    private Long depositoId;

    @NotNull
    private Long comercioId;
}
