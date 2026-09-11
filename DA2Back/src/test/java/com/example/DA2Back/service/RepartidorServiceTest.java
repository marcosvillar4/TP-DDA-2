package com.example.DA2Back.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import com.example.DA2Back.repartidor.dto.CambiarEstadoRepartidorDTO;
import com.example.DA2Back.repartidor.dto.EntregaHistorialDTO;
import com.example.DA2Back.repartidor.dto.RepartidorCreateDTO;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.dto.RepartidorUpdateDTO;
import com.example.DA2Back.repartidor.negocio.RepartidorService;

@ExtendWith(MockitoExtension.class)
class RepartidorServiceTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PedidosRepository pedidosRepository;

    @InjectMocks
    private RepartidorService repartidorService;

    private Usuario usuarioRepartidor;
    private Usuario usuarioComercio;
    private Repartidor repartidor;
    private Pedido pedidoCreado;

    @BeforeEach
    void setUp() {
        usuarioRepartidor = Usuario.builder()
                .id(1L)
                .username("Carlos Ruiz")
                .email("carlos@logired.com")
                .rol(Rol.REPARTIDOR)
                .activo(true)
                .build();

        usuarioComercio = Usuario.builder()
                .id(2L)
                .username("Urban Shoes")
                .email("urban@logired.com")
                .rol(Rol.COMERCIO)
                .activo(true)
                .build();

        repartidor = Repartidor.builder()
                .id(10L)
                .usuario(usuarioRepartidor)
                .nombre("Carlos")
                .apellido("Ruiz")
                .telefono("+54 11 5555-1111")
                .tipoVehiculo("Moto")
                .patente("ABC123")
                .zona("Palermo")
                .estado(EstadoRepartidor.DISPONIBLE)
                .activo(true)
                .build();

        pedidoCreado = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .direccionDestino("Av. Corrientes 1234")
                .estado(EstadoPedido.CREADO)
                .build();
    }

    @Test
    @DisplayName("Debe crear repartidor válido con estado DISPONIBLE y activo")
    void crearRepartidor_valido() {
        RepartidorCreateDTO dto = createDTO();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor));
        when(repartidorRepository.existsByUsuarioId(1L)).thenReturn(false);
        when(repartidorRepository.existsByPatenteIgnoreCase("ABC123")).thenReturn(false);
        when(repartidorRepository.save(any(Repartidor.class))).thenReturn(repartidor);

        RepartidorResponseDTO creado = repartidorService.crear(dto);

        assertNotNull(creado);
        assertEquals("Carlos Ruiz", creado.getNombreCompleto());
        assertEquals(EstadoRepartidor.DISPONIBLE, creado.getEstado());
        assertEquals(1L, creado.getUsuarioId());

        ArgumentCaptor<Repartidor> captor = ArgumentCaptor.forClass(Repartidor.class);
        verify(repartidorRepository).save(captor.capture());
        assertEquals(EstadoRepartidor.DISPONIBLE, captor.getValue().getEstado());
        assertEquals("ABC123", captor.getValue().getPatente());
    }

    @Test
    @DisplayName("Debe rechazar usuario inexistente")
    void crearRepartidor_usuarioInexistente() {
        RepartidorCreateDTO dto = createDTO();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> repartidorService.crear(dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar usuario con rol incorrecto")
    void crearRepartidor_rolIncorrecto() {
        RepartidorCreateDTO dto = createDTO();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioComercio));

        assertThrows(IllegalArgumentException.class, () -> repartidorService.crear(dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar usuario ya asociado")
    void crearRepartidor_usuarioYaAsociado() {
        RepartidorCreateDTO dto = createDTO();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor));
        when(repartidorRepository.existsByUsuarioId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> repartidorService.crear(dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar patente duplicada")
    void crearRepartidor_patenteDuplicada() {
        RepartidorCreateDTO dto = createDTO();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor));
        when(repartidorRepository.existsByUsuarioId(1L)).thenReturn(false);
        when(repartidorRepository.existsByPatenteIgnoreCase("ABC123")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> repartidorService.crear(dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe editar datos operativos sin cambiar usuario")
    void actualizarRepartidor() {
        RepartidorUpdateDTO dto = updateDTO();
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.existsByPatenteIgnoreCase("XYZ999")).thenReturn(false);
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepartidorResponseDTO actualizado = repartidorService.actualizar(10L, dto);

        assertEquals("Ana", actualizado.getNombre());
        assertEquals("XYZ999", actualizado.getPatente());
        assertEquals(1L, actualizado.getUsuarioId());
    }

    @Test
    @DisplayName("Debe cambiar disponibilidad")
    void cambiarDisponibilidad() {
        CambiarEstadoRepartidorDTO dto = new CambiarEstadoRepartidorDTO();
        dto.setEstado(EstadoRepartidor.NO_DISPONIBLE);

        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepartidorResponseDTO actualizado = repartidorService.cambiarEstado(10L, dto);

        assertEquals(EstadoRepartidor.NO_DISPONIBLE, actualizado.getEstado());
    }

    @Test
    @DisplayName("Debe rechazar EN_ENTREGA como cambio manual")
    void cambiarEstado_rechazaEnEntregaManual() {
        CambiarEstadoRepartidorDTO dto = new CambiarEstadoRepartidorDTO();
        dto.setEstado(EstadoRepartidor.EN_ENTREGA);

        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));

        assertThrows(IllegalStateException.class, () -> repartidorService.cambiarEstado(10L, dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar marcar DISPONIBLE si tiene pedido activo")
    void cambiarEstado_rechazaDisponibleConPedidoActivo() {
        CambiarEstadoRepartidorDTO dto = new CambiarEstadoRepartidorDTO();
        dto.setEstado(EstadoRepartidor.DISPONIBLE);
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);

        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO)
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> repartidorService.cambiarEstado(10L, dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar marcar NO_DISPONIBLE si tiene pedido activo")
    void cambiarEstado_rechazaNoDisponibleConPedidoActivo() {
        CambiarEstadoRepartidorDTO dto = new CambiarEstadoRepartidorDTO();
        dto.setEstado(EstadoRepartidor.NO_DISPONIBLE);
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);

        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO)
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> repartidorService.cambiarEstado(10L, dto));
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe realizar baja lógica")
    void desactivarRepartidor() {
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepartidorResponseDTO desactivado = repartidorService.desactivar(10L);

        assertFalse(desactivado.isActivo());
        assertEquals(EstadoRepartidor.NO_DISPONIBLE, desactivado.getEstado());
    }

    @Test
    @DisplayName("Debe rechazar baja lógica si tiene pedido activo")
    void desactivarRepartidor_conPedidoActivo() {
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);

        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO)
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> repartidorService.desactivar(10L));
        assertEquals(EstadoRepartidor.EN_ENTREGA, repartidor.getEstado());
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe asignar pedido CREADO a repartidor DISPONIBLE")
    void asignarPedido_valido() {
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findById(50L)).thenReturn(Optional.of(pedidoCreado));
        when(pedidosRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepartidorResponseDTO actualizado = repartidorService.asignarPedido(10L, 50L);

        assertEquals(EstadoRepartidor.EN_ENTREGA, actualizado.getEstado());
        assertEquals(EstadoPedido.ASIGNADO, pedidoCreado.getEstado());
        assertEquals(repartidor, pedidoCreado.getRepartidor());
    }

    @Test
    @DisplayName("Debe rechazar asignación si repartidor no está disponible")
    void asignarPedido_repartidorNoDisponible() {
        repartidor.setEstado(EstadoRepartidor.NO_DISPONIBLE);
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));

        assertThrows(IllegalStateException.class, () -> repartidorService.asignarPedido(10L, 50L));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar asignación si repartidor está inactivo")
    void asignarPedido_repartidorInactivo() {
        repartidor.setActivo(false);
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));

        assertThrows(IllegalStateException.class, () -> repartidorService.asignarPedido(10L, 50L));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar pedido ya asignado")
    void asignarPedido_pedidoYaAsignado() {
        pedidoCreado.setRepartidor(repartidor);
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findById(50L)).thenReturn(Optional.of(pedidoCreado));

        assertThrows(IllegalStateException.class, () -> repartidorService.asignarPedido(10L, 50L));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar pedido no asignable")
    void asignarPedido_pedidoNoAsignable() {
        pedidoCreado.setEstado(EstadoPedido.CANCELADO);
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findById(50L)).thenReturn(Optional.of(pedidoCreado));

        assertThrows(IllegalStateException.class, () -> repartidorService.asignarPedido(10L, 50L));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe devolver historial con pedidos finalizados")
    void obtenerHistorial_pedidosFinalizados() {
        Pedido pedidoEntregado = Pedido.builder()
                .id(60L)
                .comercioId(100L)
                .direccionDestino("Av. Santa Fe 123")
                .estado(EstadoPedido.ENTREGADO)
                .repartidor(repartidor)
                .build();
        Pedido pedidoCancelado = Pedido.builder()
                .id(61L)
                .comercioId(100L)
                .direccionDestino("Av. Callao 456")
                .estado(EstadoPedido.CANCELADO)
                .repartidor(repartidor)
                .build();

        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)
        )).thenReturn(List.of(pedidoEntregado, pedidoCancelado));

        List<EntregaHistorialDTO> historial = repartidorService.obtenerHistorial(10L);

        assertEquals(2, historial.size());
        assertEquals(60L, historial.get(0).getPedidoId());
        assertEquals(EstadoPedido.ENTREGADO, historial.get(0).getResultado());
        assertEquals(61L, historial.get(1).getPedidoId());
        assertEquals(EstadoPedido.CANCELADO, historial.get(1).getResultado());
    }

    @Test
    @DisplayName("Debe devolver detalle sin pedido actual cuando no hay pedidos activos")
    void obtenerPorId_sinPedidoActual() {
        when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findFirstByRepartidorIdAndEstadoIn(
                10L,
                List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO)
        )).thenReturn(Optional.empty());

        RepartidorResponseDTO detalle = repartidorService.obtenerPorId(10L);

        assertNull(detalle.getPedidoActual());
    }

    private RepartidorCreateDTO createDTO() {
        RepartidorCreateDTO dto = new RepartidorCreateDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Carlos");
        dto.setApellido("Ruiz");
        dto.setTelefono("+54 11 5555-1111");
        dto.setTipoVehiculo("Moto");
        dto.setPatente("ABC123");
        dto.setZona("Palermo");
        return dto;
    }

    private RepartidorUpdateDTO updateDTO() {
        RepartidorUpdateDTO dto = new RepartidorUpdateDTO();
        dto.setNombre("Ana");
        dto.setApellido("López");
        dto.setTelefono("+54 11 5555-2222");
        dto.setTipoVehiculo("Camioneta");
        dto.setPatente("XYZ999");
        dto.setZona("Belgrano");
        return dto;
    }
}
