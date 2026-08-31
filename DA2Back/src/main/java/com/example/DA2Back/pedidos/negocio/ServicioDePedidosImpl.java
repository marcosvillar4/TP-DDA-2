package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Implementacion de la interfaz ServicioDePedidos.
 * Componente de la capa de Negocio del sistema LogiRed.
 */
@Service
@RequiredArgsConstructor
public class ServicioDePedidosImpl implements ServicioDePedidos {

    private final PedidosRepository pedidosRepository;

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoDTO dto) {
        Pedido pedido = Pedido.builder()
                .comercioId(dto.getComercioId())
                .direccionDestino(dto.getDireccionDestino())
                .estado(EstadoPedido.CREADO)
                .build();

        Pedido guardado = pedidosRepository.save(pedido);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + id + " no encontrado"));
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        return pedidosRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorComercio(Long comercioId) {
        return pedidosRepository.findByComercioId(comercioId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidosRepository.findByEstado(estado)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto) {
        Pedido pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + id + " no encontrado"));

        pedido.setEstado(dto.getEstado());
        Pedido actualizado = pedidosRepository.save(pedido);
        return toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO cancelar(Long id) {
        Pedido pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + id + " no encontrado"));

        if (EstadoPedido.ENTREGADO.equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "No se puede cancelar un pedido ya entregado (id=" + id + ")");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        return toResponseDTO(pedidosRepository.save(pedido));
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .comercioId(pedido.getComercioId())
                .direccionDestino(pedido.getDireccionDestino())
                .estado(pedido.getEstado())
                .build();
    }
}