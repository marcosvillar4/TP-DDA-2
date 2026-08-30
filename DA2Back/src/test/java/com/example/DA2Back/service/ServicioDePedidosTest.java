package com.example.DA2Back.service;

import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidosImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioDePedidosTest {

    @Mock
    private PedidosRepository pedidosRepository;

    @InjectMocks
    private ServicioDePedidosImpl servicioDePedidos;

    private Pedido pedidoMock;

    @BeforeEach
    void setUp() {
        pedidoMock = Pedido.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Siempreviva 742")
                .estado(EstadoPedido.CREADO)
                .build();
    }

    @Test
    @DisplayName("Debe crear un pedido con estado CREADO exitosamente")
    void testCrearPedido_Exito() {
        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(100L);
        dto.setDireccionDestino("Av. Siempreviva 742");

        when(pedidosRepository.save(any(Pedido.class))).thenReturn(pedidoMock);

        PedidoResponseDTO resultado = servicioDePedidos.crearPedido(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(100L, resultado.getComercioId());
        assertEquals("Av. Siempreviva 742", resultado.getDireccionDestino());
        assertEquals(EstadoPedido.CREADO, resultado.getEstado());

        verify(pedidosRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debe obtener un pedido existente por ID")
    void testObtenerPorId_Exito() {
        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));

        PedidoResponseDTO resultado = servicioDePedidos.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(100L, resultado.getComercioId());
        assertEquals(EstadoPedido.CREADO, resultado.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException cuando el pedido no existe")
    void testObtenerPorId_NoEncontrado() {
        when(pedidosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> servicioDePedidos.obtenerPorId(99L));
    }

    @Test
    @DisplayName("Debe listar todos los pedidos")
    void testListarTodos() {
        when(pedidosRepository.findAll()).thenReturn(List.of(pedidoMock));

        List<PedidoResponseDTO> lista = servicioDePedidos.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).getId());
    }

    @Test
    @DisplayName("Debe actualizar el estado de un pedido exitosamente")
    void testActualizarEstado_Exito() {
        ActualizarEstadoDTO dto = new ActualizarEstadoDTO();
        dto.setEstado(EstadoPedido.EN_CAMINO);

        Pedido pedidoActualizado = Pedido.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Siempreviva 742")
                .estado(EstadoPedido.EN_CAMINO)
                .build();

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));
        when(pedidosRepository.save(any(Pedido.class))).thenReturn(pedidoActualizado);

        PedidoResponseDTO resultado = servicioDePedidos.actualizarEstado(1L, dto);

        assertNotNull(resultado);
        assertEquals(EstadoPedido.EN_CAMINO, resultado.getEstado());

        verify(pedidosRepository, times(1)).save(any(Pedido.class));
    }
}