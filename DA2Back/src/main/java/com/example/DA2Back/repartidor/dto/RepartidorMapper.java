package com.example.DA2Back.repartidor.dto;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;

public final class RepartidorMapper {

    private RepartidorMapper() {
    }

    public static Repartidor toEntity(RepartidorCreateDTO dto, Usuario usuario) {
        return Repartidor.builder()
                .usuario(usuario)
                .nombre(normalizar(dto.getNombre()))
                .apellido(normalizar(dto.getApellido()))
                .telefono(normalizar(dto.getTelefono()))
                .tipoVehiculo(normalizar(dto.getTipoVehiculo()))
                .patente(normalizar(dto.getPatente()))
                .zona(normalizar(dto.getZona()))
                .estado(EstadoRepartidor.DISPONIBLE)
                .activo(true)
                .build();
    }

    public static void applyUpdate(Repartidor repartidor, RepartidorUpdateDTO dto) {
        repartidor.setNombre(normalizar(dto.getNombre()));
        repartidor.setApellido(normalizar(dto.getApellido()));
        repartidor.setTelefono(normalizar(dto.getTelefono()));
        repartidor.setTipoVehiculo(normalizar(dto.getTipoVehiculo()));
        repartidor.setPatente(normalizar(dto.getPatente()));
        repartidor.setZona(normalizar(dto.getZona()));
    }

    public static RepartidorResponseDTO toResponseDTO(Repartidor repartidor, PedidoActualDTO pedidoActual) {
        Usuario usuario = repartidor.getUsuario();
        String nombreCompleto = repartidor.getNombre() + " " + repartidor.getApellido();

        return RepartidorResponseDTO.builder()
                .id(repartidor.getId())
                .nombre(repartidor.getNombre())
                .apellido(repartidor.getApellido())
                .nombreCompleto(nombreCompleto.trim())
                .email(usuario.getEmail())
                .telefono(repartidor.getTelefono())
                .tipoVehiculo(repartidor.getTipoVehiculo())
                .patente(repartidor.getPatente())
                .zona(repartidor.getZona())
                .estado(repartidor.getEstado())
                .activo(repartidor.isActivo())
                .usuarioId(usuario.getId())
                .pedidoActual(pedidoActual)
                .build();
    }

    private static String normalizar(String value) {
        return value == null ? null : value.trim();
    }
}
