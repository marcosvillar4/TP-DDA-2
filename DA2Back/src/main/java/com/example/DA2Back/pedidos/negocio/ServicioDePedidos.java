package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;

import java.util.List;

/**
 * Contrato de negocio del componente ServicioDePedidos,
 * segun la especificacion del diagrama de arquitectura LogiRed.
 *
 * Los controladores de la capa de presentacion dependen de esta interfaz
 * (Inversion de Control / Principio de Dependencia de Abstraccion).
 */
public interface ServicioDePedidos {

    /** Crea un nuevo pedido en estado CREADO. */
    PedidoResponseDTO crearPedido(CrearPedidoDTO dto);

    /** Retorna un pedido por su identificador unico. */
    PedidoResponseDTO obtenerPorId(Long id);

    /** Retorna todos los pedidos registrados en el sistema. */
    List<PedidoResponseDTO> listarTodos();

    /** Retorna todos los pedidos asociados a un comercio especifico. */
    List<PedidoResponseDTO> listarPorComercio(Long comercioId);

    /** Retorna todos los pedidos que se encuentren en un estado determinado. */
    List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado);

    /** Actualiza el estado de un pedido existente. */
    PedidoResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto);

    /**
     * Cancela un pedido, estableciendo su estado a CANCELADO.
     * Lanza IllegalStateException si el pedido ya fue entregado.
     */
    PedidoResponseDTO cancelar(Long id);
}