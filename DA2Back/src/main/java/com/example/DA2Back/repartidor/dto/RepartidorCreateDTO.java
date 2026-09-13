package com.example.DA2Back.repartidor.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepartidorCreateDTO {

    private Long usuarioId;
    private String patente;
    private String zona;
}
