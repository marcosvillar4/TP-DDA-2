package com.example.DA2Back.service;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidosImpl;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
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

    @Mock
    private RepartidorRepository repartidorRepository;

    @InjectMocks
    private ServicioDePedidosImpl servicioDePedidos;

    private Pedido pedidoMock;
    private Repartidor repartidor;

    @BeforeEach
    void setUp() {
        repartidor = Repartidor.builder()
                .id(10L)
                .nombre("Carlos")
                .apellido("Ruiz")
                .estado(EstadoRepartidor.EN_ENTREGA)
                .activo(true)
                .build();

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
        assertEquals(EstadoPedido.CREADO, resultado.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException cuando el pedido no existe")
    void testObtenerPorId_NoEncontrado() {
        when(pedidosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> servicioDePedidos.obtenerPorId(99L));
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
    @DisplayName("Debe listar pedidos por comercioId")
    void testListarPorComercio() {
        when(pedidosRepository.findByComercioId(100L)).thenReturn(List.of(pedidoMock));

        List<PedidoResponseDTO> lista = servicioDePedidos.listarPorComercio(100L);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(100L, lista.get(0).getComercioId());
    }

    @Test
    @DisplayName("Debe listar pedidos por estado")
    void testListarPorEstado() {
        when(pedidosRepository.findByEstado(EstadoPedido.CREADO))
                .thenReturn(List.of(pedidoMock));

        List<PedidoResponseDTO> lista =
                servicioDePedidos.listarPorEstado(EstadoPedido.CREADO);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(EstadoPedido.CREADO, lista.get(0).getEstado());
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

    @Test
    @DisplayName("Debe cancelar un pedido en estado CREADO exitosamente")
    void testCancelar_Exito() {
        Pedido pedidoCancelado = Pedido.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Siempreviva 742")
                .estado(EstadoPedido.CANCELADO)
                .build();

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));
        when(pedidosRepository.save(any(Pedido.class))).thenReturn(pedidoCancelado);

        PedidoResponseDTO resultado = servicioDePedidos.cancelar(1L);

        assertNotNull(resultado);
        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
        verify(pedidosRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException al cancelar un pedido ya entregado")
    void testCancelar_YaEntregado() {
        Pedido pedidoEntregado = Pedido.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Siempreviva 742")
                .estado(EstadoPedido.ENTREGADO)
                .build();

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedidoEntregado));

        assertThrows(IllegalStateException.class,
                () -> servicioDePedidos.cancelar(1L));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe liberar repartidor activo cuando pedido asignado pasa a ENTREGADO")
    void actualizarEstado_entregadoLiberaRepartidor() {
        ActualizarEstadoDTO dto = new ActualizarEstadoDTO();
        dto.setEstado(EstadoPedido.ENTREGADO);
        Pedido pedidoAsignado = Pedido.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Siempreviva 742")
                .estado(EstadoPedido.ASIGNADO)
                .repartidor(repartidor)
                .build();

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedidoAsignado));
        when(pedidosRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO)
        )).thenReturn(false);

        PedidoResponseDTO resultado = servicioDePedidos.actualizarEstado(1L, dto);

        assertEquals(EstadoPedido.ENTREGADO, resultado.getEstado());
        assertEquals(EstadoRepartidor.DISPONIBLE, repartidor.getEstado());
        verify(repartidorRepository).save(repartidor);
    }

    @Test
    @DisplayName("Debe liberar repartidor activo cuando pedido asignado pasa a CANCELADO")
    void cancelar_liberaRepartidor() {
        Pedido pedidoAsignado = Pedido.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Siempreviva 742")
                .estado(EstadoPedido.ASIGNADO)
                .repartidor(repartidor)
                .build();

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedidoAsignado));
        when(pedidosRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO)
        )).thenReturn(false);

        PedidoResponseDTO resultado = servicioDePedidos.cancelar(1L);

        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
        assertEquals(EstadoRepartidor.DISPONIBLE, repartidor.getEstado());
        verify(repartidorRepository).save(repartidor);
    }
}
