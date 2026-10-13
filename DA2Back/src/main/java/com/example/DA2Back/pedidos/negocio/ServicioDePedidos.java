package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import java.util.List;

public interface ServicioDePedidos {
    PedidoResponseDTO crearPedido(CrearPedidoDTO dto);
    PedidoResponseDTO obtenerPorId(Long id);
    List<PedidoResponseDTO> listarTodos();
    List<PedidoResponseDTO> listarPorComercio(Long comercioId);
    List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado);
    List<PedidoResponseDTO> listarPendientesAsignables();

    // Transiciones de estado
    PedidoResponseDTO marcarListoParaRetirar(Long id);
    PedidoResponseDTO asignarRepartidor(Long pedidoId, Long repartidorId);
    PedidoResponseDTO marcarRetirado(Long id);
    PedidoResponseDTO marcarRetiradoPorRepartidor(Long id, Long repartidorId);
    PedidoResponseDTO iniciarViaje(Long id);
    PedidoResponseDTO iniciarViajePorRepartidor(Long id, Long repartidorId);
    PedidoResponseDTO entregar(Long id);
    PedidoResponseDTO entregarPorRepartidor(Long id, Long repartidorId);
    PedidoResponseDTO cancelar(Long id);
}
