package com.example.DA2Back.controller;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.pedidos.presentacion.PedidosRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidosRestControllerTest {

    @Mock
    private ServicioDePedidos servicioDePedidos;

    @InjectMocks
    private PedidosRestController controller;

    private PedidoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = PedidoResponseDTO.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Corrientes 1234")
                .estado(EstadoPedido.CREADO)
                .build();
    }

    @Test
    @DisplayName("POST /api/pedidos debe retornar HTTP 201 Created y el DTO creado")
    void testCrearPedido() {
        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(100L);
        dto.setDireccionDestino("Av. Corrientes 1234");

        when(servicioDePedidos.crearPedido(any(CrearPedidoDTO.class))).thenReturn(responseDTO);

        ResponseEntity<PedidoResponseDTO> respuesta = controller.crearPedido(dto);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1L, respuesta.getBody().getId());
        assertEquals(100L, respuesta.getBody().getComercioId());
        assertEquals(EstadoPedido.CREADO, respuesta.getBody().getEstado());

        verify(servicioDePedidos, times(1)).crearPedido(any(CrearPedidoDTO.class));
    }

    @Test
    @DisplayName("GET /api/pedidos debe retornar HTTP 200 OK y la lista de todos los pedidos")
    void testListarTodos() {
        when(servicioDePedidos.listarTodos()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<PedidoResponseDTO>> respuesta = controller.listarTodos();

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        assertEquals(1L, respuesta.getBody().get(0).getId());

        verify(servicioDePedidos, times(1)).listarTodos();
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} debe retornar HTTP 200 OK y el pedido por ID")
    void testObtenerPorId() {
        when(servicioDePedidos.obtenerPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<PedidoResponseDTO> respuesta = controller.obtenerPorId(1L);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1L, respuesta.getBody().getId());

        verify(servicioDePedidos, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/pedidos/comercio/{id} debe retornar pedidos del comercio")
    void testListarPorComercio() {
        when(servicioDePedidos.listarPorComercio(100L)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<PedidoResponseDTO>> respuesta = controller.listarPorComercio(100L);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        assertEquals(100L, respuesta.getBody().get(0).getComercioId());

        verify(servicioDePedidos, times(1)).listarPorComercio(100L);
    }

    @Test
    @DisplayName("GET /api/pedidos/estado/{estado} debe retornar pedidos por estado")
    void testListarPorEstado() {
        when(servicioDePedidos.listarPorEstado(EstadoPedido.CREADO))
                .thenReturn(List.of(responseDTO));

        ResponseEntity<List<PedidoResponseDTO>> respuesta =
                controller.listarPorEstado(EstadoPedido.CREADO);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(EstadoPedido.CREADO, respuesta.getBody().get(0).getEstado());

        verify(servicioDePedidos, times(1)).listarPorEstado(EstadoPedido.CREADO);
    }

    @Test
    @DisplayName("PATCH /api/pedidos/{id}/estado debe retornar HTTP 200 con estado actualizado")
    void testActualizarEstado() {
        ActualizarEstadoDTO dto = new ActualizarEstadoDTO();
        dto.setEstado(EstadoPedido.EN_CAMINO);

        PedidoResponseDTO responseActualizada = PedidoResponseDTO.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Corrientes 1234")
                .estado(EstadoPedido.EN_CAMINO)
                .build();

        when(servicioDePedidos.actualizarEstado(eq(1L), any(ActualizarEstadoDTO.class)))
                .thenReturn(responseActualizada);

        ResponseEntity<PedidoResponseDTO> respuesta = controller.actualizarEstado(1L, dto);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(EstadoPedido.EN_CAMINO, respuesta.getBody().getEstado());

        verify(servicioDePedidos, times(1))
                .actualizarEstado(eq(1L), any(ActualizarEstadoDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id}/cancelar debe retornar HTTP 200 con pedido cancelado")
    void testCancelar() {
        PedidoResponseDTO responseCancelado = PedidoResponseDTO.builder()
                .id(1L)
                .comercioId(100L)
                .direccionDestino("Av. Corrientes 1234")
                .estado(EstadoPedido.CANCELADO)
                .build();

        when(servicioDePedidos.cancelar(1L)).thenReturn(responseCancelado);

        ResponseEntity<PedidoResponseDTO> respuesta = controller.cancelar(1L);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(EstadoPedido.CANCELADO, respuesta.getBody().getEstado());

        verify(servicioDePedidos, times(1)).cancelar(1L);
    }
}