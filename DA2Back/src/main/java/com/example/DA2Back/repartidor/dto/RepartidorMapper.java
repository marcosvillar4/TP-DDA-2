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
                .patente(normalizar(dto.getPatente()))
                .zona(normalizar(dto.getZona()))
                .estado(EstadoRepartidor.DISPONIBLE)
                .activo(true)
                .build();
    }

    public static void applyUpdate(Repartidor repartidor, RepartidorUpdateDTO dto) {
        repartidor.setPatente(normalizar(dto.getPatente()));
        repartidor.setZona(normalizar(dto.getZona()));
    }

    public static RepartidorResponseDTO toResponseDTO(
            Repartidor repartidor,
            PedidoActualDTO pedidoActual) {

        Usuario usuario = repartidor.getUsuario();
        String nombreCompleto = nombreCompleto(usuario);

        return RepartidorResponseDTO.builder()
                .id(repartidor.getId())
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .nombreCompleto(nombreCompleto)
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .vehiculo(usuario.getVehiculo())
                .patente(repartidor.getPatente())
                .zona(repartidor.getZona())
                .estado(repartidor.getEstado())
                .activo(repartidor.isActivo())
                .pedidoActual(pedidoActual)
                .build();
    }

    public static UsuarioRepartidorDisponibleDTO toUsuarioDisponibleDTO(Usuario usuario) {
        return UsuarioRepartidorDisponibleDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .nombreCompleto(nombreCompleto(usuario))
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .vehiculo(usuario.getVehiculo())
                .build();
    }

    private static String nombreCompleto(Usuario usuario) {
        return (usuario.getNombre() + " " + usuario.getApellido()).trim();
    }

    private static String normalizar(String value) {
        return value == null ? null : value.trim();
    }
}
