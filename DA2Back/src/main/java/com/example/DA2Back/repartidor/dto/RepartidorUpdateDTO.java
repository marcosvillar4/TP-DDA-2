package com.example.DA2Back.repartidor.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepartidorUpdateDTO {

    private String nombre;
    private String apellido;
    private String telefono;
    private String tipoVehiculo;
    private String patente;
    private String zona;
}
