package com.example.DA2Back.repartidor.negocio;

import java.util.List;

import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.repartidor.dto.CambiarEstadoRepartidorDTO;
import com.example.DA2Back.repartidor.dto.EntregaHistorialDTO;
import com.example.DA2Back.repartidor.dto.RepartidorCreateDTO;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.dto.RepartidorUpdateDTO;
import com.example.DA2Back.repartidor.dto.UsuarioRepartidorDisponibleDTO;

public interface IRepartidorService {

    List<RepartidorResponseDTO> listarTodos();

    RepartidorResponseDTO obtenerPorId(Long id);

    RepartidorResponseDTO crear(RepartidorCreateDTO dto);

    RepartidorResponseDTO actualizar(Long id, RepartidorUpdateDTO dto);

    RepartidorResponseDTO cambiarEstado(Long id, CambiarEstadoRepartidorDTO dto);

    RepartidorResponseDTO desactivar(Long id);

    RepartidorResponseDTO activar(Long id);

    RepartidorResponseDTO asignarPedido(Long repartidorId, Long pedidoId);

    List<EntregaHistorialDTO> obtenerHistorial(Long repartidorId);

    List<PedidoResponseDTO> obtenerPedidosAsignables();

    List<UsuarioRepartidorDisponibleDTO> listarUsuariosRepartidoresDisponibles();
}
