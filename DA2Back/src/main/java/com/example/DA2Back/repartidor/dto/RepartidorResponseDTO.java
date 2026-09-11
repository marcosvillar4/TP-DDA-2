package com.example.DA2Back.repartidor.dto;

import com.example.DA2Back.repartidor.dato.EstadoRepartidor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String tipoVehiculo;
    private String patente;
    private String zona;
    private EstadoRepartidor estado;
    private boolean activo;
    private Long usuarioId;
    private PedidoActualDTO pedidoActual;
}
