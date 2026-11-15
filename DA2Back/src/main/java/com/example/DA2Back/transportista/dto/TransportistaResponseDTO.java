package com.example.DA2Back.transportista.dto;

import com.example.DA2Back.transportista.dato.EstadoTransportista;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportistaResponseDTO {
    private Long id;
    private String empresa;
    private EstadoTransportista estado;
    private boolean activo;
}
