package com.example.DA2Back.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.pedidos.negocio.AccesoPedidos;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.comercio.dato.Comercio;
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

import java.util.List;

import com.example.DA2Back.pedidos.dto.DetallePedidoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.producto.dato.Producto;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;

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

    @Mock
    private com.example.DA2Back.inventario.negocio.IInventario inventarioService;

    @Mock
    private com.example.DA2Back.inventario.dato.ItemInventarioRepository itemInventarioRepository;

    @Mock
    private com.example.DA2Back.producto.dato.ProductoRepository productoRepository;

    @InjectMocks
    private ServicioDePedidosImpl servicioDePedidos;

    @Mock private AccesoPedidos accesoPedidos;
    @Mock private DepositoRepository depositoRepository;
    private final Usuario admin = Usuario.builder().id(2L).rol(Rol.ADMIN).build();

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
                .estado(EstadoRepartidor.DISPONIBLE)
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

        when(accesoPedidos.resolver(admin, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO))
                .thenReturn(new AccesoPedidos.Alcance(null, null));
        PedidoResponseDTO response = servicioDePedidos.obtenerPorId(50L, admin);

        assertEquals(10L, response.getRepartidorId());
        assertEquals("Carlos Ruiz", response.getRepartidorNombre());
    }

    @Test
    @DisplayName("cancelarPedido debe restaurar stock de los items en el inventario")
    void cancelarPedido_restauraStock() {
        com.example.DA2Back.producto.dato.Producto producto = new com.example.DA2Back.producto.dato.Producto();
        producto.setId(101L);
        producto.setNombre("Zapatillas");
        Comercio comercio = Comercio.builder().id(100L).build();
        producto.setComercio(comercio);
        Deposito deposito = new Deposito();
        deposito.setId(10L);
        deposito.setComercio(comercio);

        com.example.DA2Back.pedidos.dato.DetallePedido detalle = com.example.DA2Back.pedidos.dato.DetallePedido.builder()
                .id(1L)
                .producto(producto)
                .cantidad(3)
                .build();

        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .depositoId(10L)
                .estado(EstadoPedido.CREADO)
                .detalles(new java.util.ArrayList<>(java.util.List.of(detalle)))
                .build();

        detalle.setPedido(pedido);

        com.example.DA2Back.inventario.dato.Inventario inv = new com.example.DA2Back.inventario.dato.Inventario();
        inv.setId(5L);
        inv.setComercio(comercio);

        com.example.DA2Back.inventario.dato.ItemInventario item = new com.example.DA2Back.inventario.dato.ItemInventario();
        item.setId(20L);
        item.setCantidad(7);
        item.setInventario(inv);
        item.setProducto(producto);
        item.setDeposito(deposito);

        com.example.DA2Back.pedidos.negocio.state.IEstadoPedido estadoMock = org.mockito.Mockito.mock(com.example.DA2Back.pedidos.negocio.state.IEstadoPedido.class);

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(accesoPedidos.resolver(admin, Rol.ADMIN, Rol.COMERCIO))
                .thenReturn(new AccesoPedidos.Alcance(null, null));
        when(depositoRepository.findById(10L)).thenReturn(Optional.of(deposito));
        when(resolverEstado.resolver(EstadoPedido.CREADO)).thenReturn(estadoMock);
        when(inventarioService.obtenerPorComercio(100L)).thenReturn(inv);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(5L, 101L, 10L))
                .thenReturn(Optional.of(item));

        servicioDePedidos.cancelar(50L, admin);

        assertEquals(10, item.getCantidad());
        org.mockito.Mockito.verify(itemInventarioRepository).save(item);
        org.mockito.Mockito.verify(estadoMock).cancelar(pedido);
        org.mockito.Mockito.verify(pedidosRepository).save(pedido);
    }

    @Test
    @DisplayName("asignarRepartidor debe asignar pedido listo y cambiar repartidor a EN_ENTREGA")
    void asignarRepartidor_pedidoListo() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.LISTO_PARA_RETIRAR)
                .build();

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                java.util.List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO)))
                .thenReturn(false);
        when(resolverEstado.resolver(EstadoPedido.LISTO_PARA_RETIRAR))
                .thenReturn(new com.example.DA2Back.pedidos.negocio.state.EstadoListoParaRetirar());

        PedidoResponseDTO response = servicioDePedidos.asignarRepartidor(50L, 10L);

        assertEquals(EstadoPedido.ASIGNADO, pedido.getEstado());
        assertEquals(EstadoPedido.ASIGNADO, response.getEstado());
        assertEquals(10L, response.getRepartidorId());
        assertEquals(EstadoRepartidor.EN_ENTREGA, repartidor.getEstado());
        org.mockito.Mockito.verify(pedidosRepository).save(pedido);
        org.mockito.Mockito.verify(repartidorRepository).save(repartidor);
        org.mockito.Mockito.verify(historialRepository).save(org.mockito.Mockito.argThat(historial ->
                historial.getPedido() == pedido && historial.getEstado().name().equals(EstadoPedido.ASIGNADO.name())));
    }

    @Test
    @DisplayName("asignarRepartidor debe rechazar pedido inexistente")
    void asignarRepartidor_pedidoInexistente() {
        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> servicioDePedidos.asignarRepartidor(50L, 10L));
    }

    @Test
    @DisplayName("asignarRepartidor debe rechazar pedido que no está listo para retirar")
    void asignarRepartidor_pedidoNoDisponible() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.CREADO)
                .build();

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                java.util.List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO)))
                .thenReturn(false);
        when(resolverEstado.resolver(EstadoPedido.CREADO))
                .thenReturn(new com.example.DA2Back.pedidos.negocio.state.EstadoCreado());

        assertThrows(com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException.class,
                () -> servicioDePedidos.asignarRepartidor(50L, 10L));
    }

    @Test
    @DisplayName("asignarRepartidor debe rechazar pedido ya asignado")
    void asignarRepartidor_pedidoYaAsignado() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.LISTO_PARA_RETIRAR)
                .repartidor(repartidor)
                .build();

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));

        assertThrows(IllegalStateException.class,
                () -> servicioDePedidos.asignarRepartidor(50L, 10L));
    }

    @Test
    @DisplayName("asignarRepartidor debe rechazar repartidor inactivo")
    void asignarRepartidor_repartidorInactivo() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.LISTO_PARA_RETIRAR)
                .build();
        repartidor.setActivo(false);

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));

        assertThrows(IllegalStateException.class,
                () -> servicioDePedidos.asignarRepartidor(50L, 10L));
    }

    @Test
    @DisplayName("asignarRepartidor debe rechazar repartidor no disponible")
    void asignarRepartidor_repartidorNoDisponible() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.LISTO_PARA_RETIRAR)
                .build();
        repartidor.setEstado(EstadoRepartidor.NO_DISPONIBLE);

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));

        assertThrows(IllegalStateException.class,
                () -> servicioDePedidos.asignarRepartidor(50L, 10L));
    }

    @Test
    @DisplayName("asignarRepartidor debe rechazar repartidor con pedido activo")
    void asignarRepartidor_repartidorConPedidoActivo() {
        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.LISTO_PARA_RETIRAR)
                .build();

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(
                10L,
                java.util.List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO)))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> servicioDePedidos.asignarRepartidor(50L, 10L));
    }

    @Test
    @DisplayName("marcarRetirado debe cambiar estado de repartidor a EN_ENTREGA")
    void marcarRetirado_actualizaEstadoRepartidor() {
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);

        Pedido pedido = Pedido.builder()
                .id(50L)
                .comercioId(100L)
                .estado(EstadoPedido.ASIGNADO)
                .repartidor(repartidor)
                .build();

        com.example.DA2Back.pedidos.negocio.state.IEstadoPedido estadoMock = org.mockito.Mockito.mock(com.example.DA2Back.pedidos.negocio.state.IEstadoPedido.class);

        when(pedidosRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(repartidor));
        when(resolverEstado.resolver(EstadoPedido.ASIGNADO)).thenReturn(estadoMock);

        servicioDePedidos.marcarRetirado(50L);

        assertEquals(EstadoRepartidor.EN_ENTREGA, repartidor.getEstado());
        org.mockito.Mockito.verify(repartidorRepository).save(repartidor);
    }

    @Test
    void debeRechazarCreacionDePedidoConCantidadNegativa() {
        // DADO: un DTO de pedido con cantidad negativa (-5)
        DetallePedidoDTO detalleInvalido = new DetallePedidoDTO();
        detalleInvalido.setProductoId(1L);
        detalleInvalido.setCantidad(-5);

        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(1L);
        dto.setDepositoId(1L);
        dto.setDireccionDestino("Av. Cabildo 500");
        dto.setDetalles(List.of(detalleInvalido));

        // Usuario autenticado con rol COMERCIO que realiza la petición
        Usuario usuarioComercio = new Usuario();
        usuarioComercio.setId(1L);
        usuarioComercio.setEmail("comercio@test.com");
        usuarioComercio.setRol(Rol.COMERCIO);

        when(accesoPedidos.resolver(usuarioComercio, Rol.ADMIN, Rol.COMERCIO))
                .thenReturn(new AccesoPedidos.Alcance(1L, null));

        Deposito deposito = new Deposito();
        deposito.setId(1L);
        deposito.setComercio(Comercio.builder().id(1L).build());
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));

        lenient().when(inventarioService.obtenerPorComercio(1L)).thenReturn(new Inventario());

        // CUANDO / ENTONCES: se envía (dto, usuarioComercio), lanza IllegalArgumentException y no persiste
        assertThrows(IllegalArgumentException.class, () -> servicioDePedidos.crearPedido(dto, usuarioComercio));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("SEC-W05: Usuario con rol REPARTIDOR no puede crear pedidos (endpoint exclusivo de Comercio)")
    void secW05_repartidorNoPuedeCrearPedido() {
        // DADO: Un usuario con rol REPARTIDOR y un DTO de creación de pedido
        Usuario repartidorUser = Usuario.builder()
                .id(15L)
                .email("repartidor@test.com")
                .rol(Rol.REPARTIDOR)
                .build();

        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(1L);
        dto.setDepositoId(1L);
        dto.setDireccionDestino("Av. Corrientes 1000");

        when(accesoPedidos.resolver(repartidorUser, Rol.ADMIN, Rol.COMERCIO))
                .thenThrow(new AccessDeniedException("No tiene permiso para operar pedidos"));

        // CUANDO / ENTONCES: Se lanza AccessDeniedException y nunca se persiste
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> servicioDePedidos.crearPedido(dto, repartidorUser),
                "SEC-W05: Se debe lanzar AccessDeniedException cuando un REPARTIDOR intenta crear pedidos"
        );
        assertEquals("No tiene permiso para operar pedidos", ex.getMessage(),
                "SEC-W05: El mensaje debe indicar claramente la falta de permisos para el rol REPARTIDOR");
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("SEC-W06: Servidor valida en cada request que un Comercio no acceda a pedidos de otro Comercio")
    void secW06_comercioNoPuedeAccederAPedidoDeOtroComercio() {
        // DADO: Usuario del Comercio 1 y un pedido existente perteneciente al Comercio 2
        Usuario usuarioComercio1 = Usuario.builder()
                .id(10L)
                .email("comercio1@test.com")
                .rol(Rol.COMERCIO)
                .build();

        Pedido pedidoComercio2 = Pedido.builder()
                .id(99L)
                .comercioId(2L) // Pertenece a OTRO comercio
                .depositoId(5L)
                .estado(EstadoPedido.CREADO)
                .build();

        when(pedidosRepository.findById(99L)).thenReturn(Optional.of(pedidoComercio2));

        AccesoPedidos.Alcance alcanceComercio1 = new AccesoPedidos.Alcance(1L, null);
        when(accesoPedidos.resolver(usuarioComercio1, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO))
                .thenReturn(alcanceComercio1);

        // CUANDO / ENTONCES: al intentar obtenerlo, validarPedido() rechaza en el servidor con AccessDeniedException
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> servicioDePedidos.obtenerPorId(99L, usuarioComercio1),
                "SEC-W06: El servidor debe rechazar la consulta de un pedido que pertenece a otro comercio"
        );
        assertEquals("No tiene acceso al comercio solicitado", ex.getMessage(),
                "SEC-W06: El mensaje debe explicar que no se tiene acceso al comercio del pedido solicitado");
    }

    @Test
    @DisplayName("SEC-D02: Las respuestas exponen únicamente DTOs mínimos sin exponer la entidad JPA")
    void secD02_respuestasRetornanDtoMinimoSinExponerEntidadJPA() {
        // DADO: Pedido existente con sus campos de negocio
        Pedido pedido = Pedido.builder()
                .id(100L)
                .comercioId(1L)
                .depositoId(2L)
                .direccionDestino("Calle Falsa 123")
                .estado(EstadoPedido.CREADO)
                .detalles(new java.util.ArrayList<>())
                .historial(new java.util.ArrayList<>())
                .build();

        when(pedidosRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(accesoPedidos.resolver(admin, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO))
                .thenReturn(new AccesoPedidos.Alcance(null, null));

        // CUANDO: Se consulta el pedido
        PedidoResponseDTO responseDTO = servicioDePedidos.obtenerPorId(100L, admin);

        // ENTONCES: Se valida que retorne un DTO puro y desacoplado de la entidad JPA con mensaje explicativo
        assertNotNull(responseDTO, "SEC-D02: La respuesta debe ser un objeto DTO válido y no nulo");
        assertEquals(100L, responseDTO.getId(), "SEC-D02: El DTO debe exponer el identificador del pedido");
        assertEquals(1L, responseDTO.getComercioId(), "SEC-D02: El DTO debe limitar los datos al comercio asociado");
        assertEquals(2L, responseDTO.getDepositoId(), "SEC-D02: El DTO debe limitar los datos al depósito asociado");
        assertEquals(EstadoPedido.CREADO, responseDTO.getEstado(), "SEC-D02: El DTO debe exponer solo el estado operativo");
        assertEquals("Calle Falsa 123", responseDTO.getDireccionDestino(),
                "SEC-D02: El DTO expone los datos estrictamente necesarios de destino sin filtrar entidades JPA internas");
    }

}
