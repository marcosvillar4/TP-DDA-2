package com.example.DA2Back.transportista.dto;

import com.example.DA2Back.transportista.dato.EstadoTransportista;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CambiarEstadoTransportistaDTO {
    private EstadoTransportista estado;
}
