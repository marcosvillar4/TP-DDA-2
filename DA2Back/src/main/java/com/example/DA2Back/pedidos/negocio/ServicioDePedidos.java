package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;

import java.util.List;

/**
 * Interfaz del componente de negocio ServicioDePedidos,
 * segun la especificacion del diagrama de arquitectura LogiRed.
 */
public interface ServicioDePedidos {

    PedidoResponseDTO crearPedido(CrearPedidoDTO dto);

    PedidoResponseDTO obtenerPorId(Long id);

    List<PedidoResponseDTO> listarTodos();

    PedidoResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto);
}