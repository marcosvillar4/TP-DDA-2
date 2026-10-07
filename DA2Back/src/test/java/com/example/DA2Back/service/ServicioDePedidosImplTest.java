package com.example.DA2Back.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedidoRepository;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidosImpl;
import com.example.DA2Back.pedidos.negocio.state.ResolverEstadoPedido;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;

@ExtendWith(MockitoExtension.class)
class ServicioDePedidosImplTest {

    @Mock
    private PedidosRepository pedidosRepository;

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistorialEstadoPedidoRepository historialRepository;

    @Mock
    private ResolverEstadoPedido resolverEstado;

    @InjectMocks
    private ServicioDePedidosImpl servicioDePedidos;

    private Usuario usuarioRepartidor;
    private Repartidor repartidor;

    @BeforeEach
    void setUp() {
        usuarioRepartidor = Usuario.builder()
                .id(1L)
                .nombre("Carlos")
                .apellido("Ruiz")
                .build();

        repartidor = Repartidor.builder()
                .id(10L)
                .usuarioId(1L)
                .patente("ABC123")
                .zona("Palermo")
                .estado(EstadoRepartidor.EN_ENTREGA)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("PedidoResponseDTO debe conservar repartidorNombre resolviendo Usuario por usuarioId")
    void obtenerPorId_mapeaNombreDeRepartidorDesdeUsuarioId() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .direccionDestino("Av. Corrientes 1234")
                .estado(EstadoPedido.ASIGNADO)
                .repartidor(repartidor)
                .build();

        when(pedidosRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor));

        PedidoResponseDTO response = servicioDePedidos.obtenerPorId(50L);

        assertEquals(10L, response.getRepartidorId());
        assertEquals("Carlos Ruiz", response.getRepartidorNombre());
    }
}
