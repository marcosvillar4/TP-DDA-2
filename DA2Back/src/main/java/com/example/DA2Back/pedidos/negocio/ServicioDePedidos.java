package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;

import java.util.List;

/**
 * Contrato de negocio del componente ServicioDePedidos.
 *
 * Es la ÚNICA interfaz a través de la cual se puede crear o mutar un Pedido.
 * Los controladores y otros servicios (ej. RepartidorService) dependen de
 * esta abstracción, nunca del repositorio directamente (Inversión de Control).
 *
 * Las transiciones de estado son validadas internamente por el patrón State
 * (ResolverEstadoPedido). Si una transición no es válida desde el estado actual
 * del pedido, se lanza TransicionInvalidaPedidoException.
 */
public interface ServicioDePedidos {

    /** Crea un nuevo pedido en estado CREADO. */
    PedidoResponseDTO crearPedido(CrearPedidoDTO dto);

    /** Retorna un pedido por su identificador único. */
    PedidoResponseDTO obtenerPorId(Long id);

    /** Retorna todos los pedidos registrados en el sistema. */
    List<PedidoResponseDTO> listarTodos();

    /** Retorna todos los pedidos asociados a un comercio específico. */
    List<PedidoResponseDTO> listarPorComercio(Long comercioId);

    /** Retorna todos los pedidos que se encuentren en un estado determinado. */
    List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado);

    /** Retorna pedidos CREADO que todavía no tienen repartidor asignado. */
    List<PedidoResponseDTO> listarPendientesAsignables();

    /**
     * Asigna un repartidor al pedido: CREADO → ASIGNADO.
     * Es el punto único de asignación — otros servicios no deben
     * modificar el estado del pedido directamente.
     */
    PedidoResponseDTO asignarRepartidor(Long pedidoId, Long repartidorId);

    /** El repartidor inicia el viaje: ASIGNADO → EN_CAMINO. */
    PedidoResponseDTO iniciarViaje(Long id);

    /** El repartidor confirma la entrega: EN_CAMINO → ENTREGADO. */
    PedidoResponseDTO entregar(Long id);

    /**
     * Cancela el pedido desde cualquier estado que lo permita.
     * Libera al repartidor si corresponde.
     */
    PedidoResponseDTO cancelar(Long id);
}
