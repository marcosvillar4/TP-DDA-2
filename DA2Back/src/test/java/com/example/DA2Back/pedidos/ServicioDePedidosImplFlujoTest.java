package com.example.DA2Back.pedidos;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.cotizacion.dto.CotizacionDTO;
import com.example.DA2Back.cotizacion.dto.PuntoDTO;
import com.example.DA2Back.cotizacion.negocio.ServicioDeCotizacion;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.inventario.negocio.IInventario;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import com.example.DA2Back.pedidos.dato.DetallePedido;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedidoRepository;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.DetallePedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.pedidos.negocio.AccesoPedidos;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidosImpl;
import com.example.DA2Back.pedidos.negocio.evento.PedidoListoParaRetirarEvent;
import com.example.DA2Back.pedidos.negocio.state.EstadoAsignado;
import com.example.DA2Back.pedidos.negocio.state.EstadoCancelado;
import com.example.DA2Back.pedidos.negocio.state.EstadoCreado;
import com.example.DA2Back.pedidos.negocio.state.EstadoEnCamino;
import com.example.DA2Back.pedidos.negocio.state.EstadoEntregado;
import com.example.DA2Back.pedidos.negocio.state.EstadoListoParaRetirar;
import com.example.DA2Back.pedidos.negocio.state.EstadoRetirado;
import com.example.DA2Back.pedidos.negocio.state.ResolverEstadoPedido;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del servicio de pedidos con el patron State REAL (no mockeado) y el control de acceso real.
 * Cubren creacion con descuento de stock, consultas por alcance, todo el ciclo de vida y la cancelacion
 * con restitucion de stock y liberacion del repartidor.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServicioDePedidosImplFlujoTest {

    private static final List<EstadoPedido> ACTIVOS =
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO);

    @Mock private PedidosRepository pedidosRepository;
    @Mock private RepartidorRepository repartidorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialEstadoPedidoRepository historialRepository;
    @Mock private IInventario inventarioService;
    @Mock private ItemInventarioRepository itemInventarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private DepositoRepository depositoRepository;
    @Mock private ComercioRepository comercioRepository;
    @Mock private NotificacionService notificacionService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ServicioDeCotizacion cotizacionService;

    private ServicioDePedidosImpl servicio;

    private final Comercio comercio1 = Comercio.builder().id(1L).usuarioId(10L).build();
    private final Deposito deposito3 = deposito(3L, comercio1);
    private final Usuario admin = usuario(2L, Rol.ADMIN);
    private final Usuario dueno = usuario(10L, Rol.COMERCIO);
    private final Usuario duenoAjeno = usuario(11L, Rol.COMERCIO);
    private final Usuario encargadoDeposito = usuario(30L, Rol.DEPOSITO);

    @BeforeEach
    void setUp() {
        ResolverEstadoPedido resolver = new ResolverEstadoPedido(new EstadoCreado(), new EstadoListoParaRetirar(),
                new EstadoAsignado(), new EstadoRetirado(), new EstadoEnCamino(), new EstadoEntregado(),
                new EstadoCancelado());
        AccesoPedidos acceso = new AccesoPedidos(comercioRepository, depositoRepository);
        servicio = new ServicioDePedidosImpl(pedidosRepository, repartidorRepository, usuarioRepository,
                historialRepository, resolver, inventarioService, itemInventarioRepository, productoRepository,
                depositoRepository, acceso, notificacionService, eventPublisher, cotizacionService);

        when(comercioRepository.findByUsuarioId(10L)).thenReturn(Optional.of(comercio1));
        when(comercioRepository.findByUsuarioId(11L))
                .thenReturn(Optional.of(Comercio.builder().id(2L).usuarioId(11L).build()));
        when(depositoRepository.findById(3L)).thenReturn(Optional.of(deposito3));
        when(depositoRepository.findByUsuarioId(30L)).thenReturn(Optional.of(deposito3));
        when(pedidosRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        when(itemInventarioRepository.save(any(ItemInventario.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ═════════════════════════ crearPedido ═════════════════════════

    @Test
    @DisplayName("Crear pedido: descuenta stock, cotiza, guarda como CREADO, registra historial y avisa stock minimo")
    void crear_ok_descuentaStockYCotiza() {
        Stock s = stock(10);
        when(cotizacionService.cotizar(3L, "Origen 1", "Destino 2", 2)).thenReturn(cotizacion(2));
        when(pedidosRepository.save(any(Pedido.class))).thenAnswer(i -> {
            Pedido p = i.getArgument(0);
            p.setId(99L);
            return p;
        });

        PedidoResponseDTO res = servicio.crearPedido(dto(1L, 3L, detalle(7L, 2)), dueno);

        assertEquals(99L, res.getId());
        assertEquals(EstadoPedido.CREADO, res.getEstado());
        assertEquals(new BigDecimal("160"), res.getCostoEnvio());
        assertEquals("Origen cotizado", res.getDireccionOrigen());
        assertEquals("Destino cotizado", res.getDireccionDestino());
        assertEquals(1, res.getDetalles().size());
        assertEquals(8, s.item.getCantidad());
        verify(itemInventarioRepository).save(s.item);
        verify(notificacionService).notificarSiCruzoStockMinimo(s.item, 10, 8);
        verify(historialRepository).save(any(HistorialEstadoPedido.class));
        verify(notificacionService, never()).notificarActualizacionPedido(any(), any());
    }

    @Test
    @DisplayName("Crear pedido pidiendo exactamente el stock disponible esta permitido (deja 0)")
    void crear_stockExacto_permitido() {
        Stock s = stock(2);
        when(cotizacionService.cotizar(eq(3L), any(), any(), anyInt())).thenReturn(cotizacion(2));

        servicio.crearPedido(dto(1L, 3L, detalle(7L, 2)), dueno);

        assertEquals(0, s.item.getCantidad());
    }

    @Test
    @DisplayName("Crear pedido: stock insuficiente -> IllegalState, sin descontar ni guardar nada")
    void crear_stockInsuficiente() {
        Stock s = stock(1);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 2)), dueno));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        assertEquals(1, s.item.getCantidad());
        verify(itemInventarioRepository, never()).save(any());
        verify(pedidosRepository, never()).save(any());
        verify(cotizacionService, never()).cotizar(any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Crear pedido: producto sin item de stock en el deposito -> IllegalState")
    void crear_sinStockEnDeposito() {
        Inventario inv = inventario(20L, comercio1);
        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inv);
        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto(7L, "Zapatillas", comercio1)));
        when(itemInventarioRepository.buscarParaActualizarStock(20L, 7L, 3L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 1)), dueno));
    }

    @Test
    @DisplayName("Crear pedido: producto inexistente -> NoSuchElement")
    void crear_productoInexistente() {
        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inventario(20L, comercio1));
        when(productoRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 1)), dueno));
    }

    @Test
    @DisplayName("Crear pedido: producto, inventario o deposito de otro comercio -> AccessDenied")
    void crear_entidadesDeOtroComercio() {
        Comercio otro = Comercio.builder().id(2L).build();
        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inventario(20L, comercio1));
        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto(7L, "Ajeno", otro)));
        assertThrows(AccessDeniedException.class,
                () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 1)), dueno));

        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inventario(20L, otro));
        assertThrows(AccessDeniedException.class,
                () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 1)), dueno));

        Producto propio = producto(7L, "Propio", comercio1);
        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inventario(20L, comercio1));
        when(productoRepository.findById(7L)).thenReturn(Optional.of(propio));
        ItemInventario conDepositoAjeno = item(inventario(20L, comercio1), propio, deposito(3L, otro), 10);
        when(itemInventarioRepository.buscarParaActualizarStock(20L, 7L, 3L)).thenReturn(Optional.of(conDepositoAjeno));
        assertThrows(AccessDeniedException.class,
                () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 1)), dueno));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear pedido: deposito inexistente, de otro comercio o comercio/deposito faltantes")
    void crear_depositoInvalido() {
        when(depositoRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.crearPedido(dto(1L, 404L), dueno));

        when(depositoRepository.findById(5L)).thenReturn(Optional.of(deposito(5L, Comercio.builder().id(2L).build())));
        assertThrows(AccessDeniedException.class, () -> servicio.crearPedido(dto(1L, 5L), dueno));

        // ADMIN sin comercioId en el DTO: no hay comercio contra el cual validar
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto(null, 3L), admin));
        // deposito faltante
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto(1L, null), dueno));
    }

    @Test
    @DisplayName("Crear pedido: usuario nulo, rol no permitido o comercio ajeno -> AccessDenied; dto nulo -> IllegalArgument")
    void crear_accesoYDto() {
        assertThrows(AccessDeniedException.class, () -> servicio.crearPedido(dto(1L, 3L), null));
        assertThrows(AccessDeniedException.class, () -> servicio.crearPedido(dto(1L, 3L), encargadoDeposito));
        assertThrows(AccessDeniedException.class, () -> servicio.crearPedido(dto(1L, 3L), duenoAjeno));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(null, dueno));
    }

    @Test
    @DisplayName("Crear pedido: detalle nulo, sin producto o con cantidad <= 0 se rechaza")
    void crear_detallesInvalidos() {
        CrearPedidoDTO conNulo = dto(1L, 3L);
        conNulo.setDetalles(java.util.Arrays.asList((DetallePedidoDTO) null));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(conNulo, dueno));

        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto(1L, 3L, detalle(null, 1)), dueno));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, null)), dueno));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, 0)), dueno));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto(1L, 3L, detalle(7L, -1)), dueno));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear pedido: la suma de cantidades del mismo producto fuera de rango se rechaza")
    void crear_cantidadFueraDeRango() {
        CrearPedidoDTO dto = dto(1L, 3L, detalle(7L, Integer.MAX_VALUE), detalle(7L, 1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> servicio.crearPedido(dto, dueno));

        assertEquals("Cantidad fuera de rango", ex.getMessage());
    }

    @Test
    @DisplayName("Crear pedido sin detalles no consulta inventario y cotiza con 0 objetos")
    void crear_sinDetalles() {
        when(cotizacionService.cotizar(eq(3L), any(), any(), eq(0))).thenReturn(cotizacion(0));

        PedidoResponseDTO res = servicio.crearPedido(dto(1L, 3L), dueno);

        assertEquals(EstadoPedido.CREADO, res.getEstado());
        verify(inventarioService, never()).obtenerPorComercio(any());
        verify(itemInventarioRepository, never()).save(any());
    }

    // ═════════════════════════ cotizarPedido ═════════════════════════

    @Test
    @DisplayName("Cotizar suma las cantidades de todos los detalles y no toca stock ni guarda")
    void cotizar_sumaCantidades() {
        when(cotizacionService.cotizar(3L, "A", "B", 5)).thenReturn(cotizacion(5));
        CrearPedidoDTO dto = dto(1L, 3L, detalle(7L, 2), detalle(8L, 3));
        dto.setDireccionOrigen("A");
        dto.setDireccionDestino("B");

        CotizacionDTO res = servicio.cotizarPedido(dto, dueno);

        assertEquals(5, res.cantidadObjetos());
        verify(pedidosRepository, never()).save(any());
        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cotizar: dto nulo, cantidad <= 0, detalle nulo o fuera de rango -> IllegalArgument; sin permiso -> AccessDenied")
    void cotizar_invalidos() {
        assertThrows(IllegalArgumentException.class, () -> servicio.cotizarPedido(null, dueno));
        assertThrows(IllegalArgumentException.class, () -> servicio.cotizarPedido(dto(1L, 3L, detalle(7L, 0)), dueno));
        assertThrows(IllegalArgumentException.class, () -> servicio.cotizarPedido(dto(1L, 3L, detalle(7L, null)), dueno));
        CrearPedidoDTO conNulo = dto(1L, 3L);
        conNulo.setDetalles(java.util.Arrays.asList((DetallePedidoDTO) null));
        assertThrows(IllegalArgumentException.class, () -> servicio.cotizarPedido(conNulo, dueno));
        assertThrows(IllegalArgumentException.class, () -> servicio.cotizarPedido(
                dto(1L, 3L, detalle(7L, Integer.MAX_VALUE), detalle(8L, Integer.MAX_VALUE)), dueno));
        assertThrows(AccessDeniedException.class, () -> servicio.cotizarPedido(dto(1L, 3L), encargadoDeposito));
        assertThrows(AccessDeniedException.class, () -> servicio.cotizarPedido(dto(1L, 3L), duenoAjeno));
    }

    // ═════════════════════════ consultas ═════════════════════════

    @Test
    @DisplayName("obtenerPorId: inexistente -> NoSuchElement; de otro comercio -> AccessDenied; propio -> ok")
    void obtenerPorId() {
        when(pedidosRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.obtenerPorId(404L, admin));

        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        when(pedidosRepository.findById(5L)).thenReturn(Optional.of(p));
        assertThrows(AccessDeniedException.class, () -> servicio.obtenerPorId(5L, duenoAjeno));
        assertEquals(5L, servicio.obtenerPorId(5L, dueno).getId());
        assertEquals(5L, servicio.obtenerPorId(5L, encargadoDeposito).getId());
    }

    @Test
    @DisplayName("Un DEPOSITO no ve pedidos de otro deposito del mismo comercio")
    void obtenerPorId_depositoAjeno() {
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        p.setDepositoId(99L);
        when(pedidosRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThrows(AccessDeniedException.class, () -> servicio.obtenerPorId(5L, encargadoDeposito));
    }

    @Test
    @DisplayName("Listados: cada rol consulta con su alcance (ADMIN global, COMERCIO por comercio, DEPOSITO por deposito)")
    void listados_porAlcance() {
        when(pedidosRepository.buscarEnAlcance(any(), any(), any(), anyBoolean())).thenReturn(List.of());

        servicio.listarTodos(admin);
        verify(pedidosRepository).buscarEnAlcance(null, null, null, false);

        servicio.listarTodos(dueno);
        verify(pedidosRepository).buscarEnAlcance(1L, null, null, false);

        servicio.listarTodos(encargadoDeposito);
        verify(pedidosRepository).buscarEnAlcance(1L, 3L, null, false);

        servicio.listarPorEstado(EstadoPedido.ENTREGADO, admin);
        verify(pedidosRepository).buscarEnAlcance(null, null, EstadoPedido.ENTREGADO, false);

        servicio.listarPendientesAsignables(admin);
        verify(pedidosRepository).buscarEnAlcance(null, null, EstadoPedido.LISTO_PARA_RETIRAR, true);
    }

    @Test
    @DisplayName("listarPorComercio: ADMIN filtra por el comercio pedido; un comercio ajeno -> AccessDenied")
    void listarPorComercio() {
        when(pedidosRepository.buscarEnAlcance(any(), any(), any(), anyBoolean())).thenReturn(List.of());

        servicio.listarPorComercio(7L, admin);
        verify(pedidosRepository).buscarEnAlcance(7L, null, null, false);

        assertThrows(AccessDeniedException.class, () -> servicio.listarPorComercio(2L, dueno));
        assertThrows(AccessDeniedException.class, () -> servicio.listarTodos(usuario(77L, Rol.REPARTIDOR)));
    }

    @Test
    @DisplayName("Listado devuelve los pedidos mapeados a DTO; sin usuario: pendientes asignables globales")
    void listados_mapeoYPendientes() {
        Pedido p = pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null);
        when(pedidosRepository.buscarEnAlcance(null, null, null, false)).thenReturn(List.of(p));
        when(pedidosRepository.findByEstadoAndRepartidorIsNull(EstadoPedido.LISTO_PARA_RETIRAR)).thenReturn(List.of(p));

        assertEquals(5L, servicio.listarTodos(admin).get(0).getId());
        assertEquals(1, servicio.listarPendientesAsignables().size());
    }

    // ═════════════════════════ marcarListoParaRetirar ═════════════════════════

    @Test
    @DisplayName("Marcar listo: CREADO -> LISTO_PARA_RETIRAR, guarda, audita, notifica y publica el evento")
    void marcarListo_ok() {
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        bloquear(p);

        PedidoResponseDTO res = servicio.marcarListoParaRetirar(5L, dueno);

        assertEquals(EstadoPedido.LISTO_PARA_RETIRAR, res.getEstado());
        verify(pedidosRepository).save(p);
        verify(historialRepository).save(any(HistorialEstadoPedido.class));
        verify(notificacionService).notificarActualizacionPedido(p, EstadoPedido.LISTO_PARA_RETIRAR);
        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(evento.capture());
        assertEquals(5L, assertInstanceOf(PedidoListoParaRetirarEvent.class, evento.getValue()).pedidoId());
    }

    @Test
    @DisplayName("Marcar listo desde un estado invalido -> TransicionInvalida y NO se publica el evento")
    void marcarListo_estadoInvalido() {
        Pedido p = pedido(5L, EstadoPedido.ENTREGADO, null);
        bloquear(p);

        assertThrows(TransicionInvalidaPedidoException.class, () -> servicio.marcarListoParaRetirar(5L, dueno));

        verify(pedidosRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Marcar listo: pedido inexistente, de otro comercio o con deposito ajeno")
    void marcarListo_errores() {
        when(pedidosRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.marcarListoParaRetirar(404L, dueno));

        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        bloquear(p);
        assertThrows(AccessDeniedException.class, () -> servicio.marcarListoParaRetirar(5L, duenoAjeno));

        Pedido conDepositoAjeno = pedido(6L, EstadoPedido.CREADO, null);
        conDepositoAjeno.setDepositoId(5L);
        bloquear(conDepositoAjeno);
        when(depositoRepository.findById(5L)).thenReturn(Optional.of(deposito(5L, Comercio.builder().id(2L).build())));
        assertThrows(AccessDeniedException.class, () -> servicio.marcarListoParaRetirar(6L, dueno));
    }

    // ═════════════════════════ asignarRepartidor ═════════════════════════

    @Test
    @DisplayName("Asignar: LISTO_PARA_RETIRAR -> ASIGNADO; el repartidor pasa a EN_ENTREGA y se notifica")
    void asignar_ok() {
        Pedido p = pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null);
        Repartidor r = repartidor(10L, EstadoRepartidor.DISPONIBLE, true);
        bloquear(p);
        bloquear(r);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor(Rol.REPARTIDOR, EstadoUsuario.VALIDADO)));

        PedidoResponseDTO res = servicio.asignarRepartidor(5L, 10L);

        assertEquals(EstadoPedido.ASIGNADO, res.getEstado());
        assertEquals(10L, res.getRepartidorId());
        assertEquals(EstadoRepartidor.EN_ENTREGA, r.getEstado());
        verify(repartidorRepository).save(r);
        verify(pedidosRepository).save(p);
        verify(notificacionService).notificarPedidoAsignado(p, r);
        verify(notificacionService).notificarActualizacionPedido(p, EstadoPedido.ASIGNADO);
    }

    @Test
    @DisplayName("Asignar: pedido o repartidor inexistentes -> NoSuchElement")
    void asignar_inexistentes() {
        when(pedidosRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.asignarRepartidor(404L, 10L));

        bloquear(pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null));
        when(repartidorRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.asignarRepartidor(5L, 404L));
    }

    @Test
    @DisplayName("Asignar: el usuario del repartidor debe existir, tener rol REPARTIDOR y estar VALIDADO")
    void asignar_usuarioRepartidorInvalido() {
        bloquear(pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null));
        bloquear(repartidor(10L, EstadoRepartidor.DISPONIBLE, true));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> servicio.asignarRepartidor(5L, 10L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor(Rol.COMERCIO, EstadoUsuario.VALIDADO)));
        assertThrows(IllegalStateException.class, () -> servicio.asignarRepartidor(5L, 10L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor(Rol.REPARTIDOR, EstadoUsuario.EN_EVALUACION)));
        assertThrows(IllegalStateException.class, () -> servicio.asignarRepartidor(5L, 10L));
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Asignar: un pedido que no esta LISTO_PARA_RETIRAR se rechaza y el repartidor queda DISPONIBLE")
    void asignar_estadoDelPedidoInvalido() {
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        Repartidor r = repartidor(10L, EstadoRepartidor.DISPONIBLE, true);
        bloquear(p);
        bloquear(r);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioRepartidor(Rol.REPARTIDOR, EstadoUsuario.VALIDADO)));

        assertThrows(TransicionInvalidaPedidoException.class, () -> servicio.asignarRepartidor(5L, 10L));

        assertEquals(EstadoRepartidor.DISPONIBLE, r.getEstado());
        verify(repartidorRepository, never()).save(any());
    }

    // ═════════════════════════ retiro, viaje y entrega ═════════════════════════

    @Test
    @DisplayName("Retirar: ASIGNADO -> RETIRADO, audita y deja al repartidor EN_ENTREGA")
    void retirar_ok() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.ASIGNADO, r);
        bloquear(p);
        bloquear(r);

        PedidoResponseDTO res = servicio.marcarRetirado(5L);

        assertEquals(EstadoPedido.RETIRADO, res.getEstado());
        assertEquals(EstadoRepartidor.EN_ENTREGA, r.getEstado());
        verify(historialRepository).save(any(HistorialEstadoPedido.class));
        verify(notificacionService).notificarActualizacionPedido(p, EstadoPedido.RETIRADO);
    }

    @Test
    @DisplayName("Retirar/viajar/entregar 'por repartidor' solo lo puede hacer el repartidor asignado")
    void porRepartidor_soloElAsignado() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.ASIGNADO, r);
        bloquear(p);
        bloquear(r);

        assertThrows(AccessDeniedException.class, () -> servicio.marcarRetiradoPorRepartidor(5L, 99L));
        assertThrows(AccessDeniedException.class, () -> servicio.iniciarViajePorRepartidor(5L, 99L));
        assertThrows(AccessDeniedException.class, () -> servicio.entregarPorRepartidor(5L, 99L));
        assertEquals(EstadoPedido.ASIGNADO, p.getEstado());

        assertEquals(EstadoPedido.RETIRADO, servicio.marcarRetiradoPorRepartidor(5L, 10L).getEstado());
        assertEquals(EstadoPedido.EN_CAMINO, servicio.iniciarViajePorRepartidor(5L, 10L).getEstado());
        assertEquals(EstadoPedido.ENTREGADO, servicio.entregarPorRepartidor(5L, 10L).getEstado());
    }

    @Test
    @DisplayName("Iniciar viaje: RETIRADO -> EN_CAMINO")
    void iniciarViaje_ok() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.RETIRADO, r);
        bloquear(p);
        bloquear(r);

        assertEquals(EstadoPedido.EN_CAMINO, servicio.iniciarViaje(5L).getEstado());
        verify(notificacionService).notificarActualizacionPedido(p, EstadoPedido.EN_CAMINO);
    }

    @Test
    @DisplayName("Entregar: EN_CAMINO -> ENTREGADO y el repartidor vuelve a DISPONIBLE si no tiene otro pedido activo")
    void entregar_liberaRepartidor() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.EN_CAMINO, r);
        bloquear(p);
        bloquear(r);
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(10L, ACTIVOS)).thenReturn(false);

        PedidoResponseDTO res = servicio.entregar(5L);

        assertEquals(EstadoPedido.ENTREGADO, res.getEstado());
        assertEquals(EstadoRepartidor.DISPONIBLE, r.getEstado());
        verify(repartidorRepository, times(1)).save(r);
    }

    @Test
    @DisplayName("Entregar con otro pedido activo: el repartidor NO se libera")
    void entregar_conOtroPedidoActivo_noLibera() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.EN_CAMINO, r);
        bloquear(p);
        bloquear(r);
        when(pedidosRepository.existsByRepartidorIdAndEstadoIn(10L, ACTIVOS)).thenReturn(true);

        servicio.entregar(5L);

        assertEquals(EstadoRepartidor.EN_ENTREGA, r.getEstado());
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Entregar un pedido que no esta EN_CAMINO -> TransicionInvalida, sin guardar")
    void entregar_estadoInvalido() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.ASIGNADO, r);
        bloquear(p);
        bloquear(r);

        assertThrows(TransicionInvalidaPedidoException.class, () -> servicio.entregar(5L));

        assertEquals(EstadoPedido.ASIGNADO, p.getEstado());
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Operar un pedido sin repartidor, con repartidor inexistente, inactivo o no EN_ENTREGA se rechaza")
    void operar_repartidorNoOperativo() {
        when(pedidosRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.marcarRetirado(404L));

        Pedido sinRepartidor = pedido(5L, EstadoPedido.ASIGNADO, null);
        bloquear(sinRepartidor);
        assertThrows(IllegalStateException.class, () -> servicio.marcarRetirado(5L));

        Repartidor inexistente = repartidor(11L, EstadoRepartidor.EN_ENTREGA, true);
        bloquear(pedido(6L, EstadoPedido.ASIGNADO, inexistente));
        when(repartidorRepository.findByIdForUpdate(11L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.marcarRetirado(6L));

        Repartidor inactivo = repartidor(12L, EstadoRepartidor.EN_ENTREGA, false);
        bloquear(pedido(7L, EstadoPedido.ASIGNADO, inactivo));
        bloquear(inactivo);
        assertThrows(IllegalStateException.class, () -> servicio.marcarRetirado(7L));

        Repartidor libre = repartidor(13L, EstadoRepartidor.DISPONIBLE, true);
        bloquear(pedido(8L, EstadoPedido.ASIGNADO, libre));
        bloquear(libre);
        assertThrows(IllegalStateException.class, () -> servicio.marcarRetirado(8L));
    }

    // ═════════════════════════ cancelar ═════════════════════════

    @Test
    @DisplayName("Cancelar un pedido CREADO sin detalles: queda CANCELADO, audita y notifica")
    void cancelar_ok_sinDetalles() {
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        bloquear(p);

        PedidoResponseDTO res = servicio.cancelar(5L, dueno);

        assertEquals(EstadoPedido.CANCELADO, res.getEstado());
        verify(historialRepository).save(any(HistorialEstadoPedido.class));
        verify(notificacionService).notificarActualizacionPedido(p, EstadoPedido.CANCELADO);
    }

    @Test
    @DisplayName("Cancelar restituye al stock exactamente las cantidades del pedido")
    void cancelar_restituyeStock() {
        Stock s = stock(8);
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        p.getDetalles().add(DetallePedido.builder().pedido(p).producto(s.producto).cantidad(2).build());
        bloquear(p);

        servicio.cancelar(5L, admin);

        assertEquals(10, s.item.getCantidad());
        verify(itemInventarioRepository).save(s.item);
    }

    @Test
    @DisplayName("Cancelar un pedido ASIGNADO libera al repartidor")
    void cancelar_liberaRepartidor() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        Pedido p = pedido(5L, EstadoPedido.ASIGNADO, r);
        bloquear(p);
        bloquear(r);

        servicio.cancelar(5L, dueno);

        assertEquals(EstadoRepartidor.DISPONIBLE, r.getEstado());
        verify(repartidorRepository).save(r);
    }

    @Test
    @DisplayName("Cancelar: repartidor asignado inexistente -> IllegalState")
    void cancelar_repartidorInexistente() {
        Repartidor r = repartidor(10L, EstadoRepartidor.EN_ENTREGA, true);
        bloquear(pedido(5L, EstadoPedido.ASIGNADO, r));
        when(repartidorRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> servicio.cancelar(5L, dueno));
    }

    @Test
    @DisplayName("Cancelar un pedido ENTREGADO o ya CANCELADO -> TransicionInvalida y el stock no se toca")
    void cancelar_estadosFinales() {
        Stock s = stock(8);
        for (EstadoPedido final_ : List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)) {
            Pedido p = pedido(5L, final_, null);
            p.getDetalles().add(DetallePedido.builder().pedido(p).producto(s.producto).cantidad(2).build());
            bloquear(p);

            assertThrows(TransicionInvalidaPedidoException.class, () -> servicio.cancelar(5L, admin));
            assertEquals(final_, p.getEstado());
        }
        assertEquals(8, s.item.getCantidad());
        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelar: usuario sin permiso o pedido de otro comercio -> AccessDenied; inexistente -> NoSuchElement")
    void cancelar_acceso() {
        when(pedidosRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.cancelar(404L, admin));

        bloquear(pedido(5L, EstadoPedido.CREADO, null));
        assertThrows(AccessDeniedException.class, () -> servicio.cancelar(5L, duenoAjeno));
        assertThrows(AccessDeniedException.class, () -> servicio.cancelar(5L, encargadoDeposito));
        assertThrows(AccessDeniedException.class, () -> servicio.cancelar(5L, null));
    }

    @Test
    @DisplayName("Cancelar: detalles incompletos (cantidad <= 0, sin producto) no permiten restituir stock")
    void cancelar_detallesIncompletos() {
        Stock s = stock(8);
        Pedido conCantidadCero = pedido(5L, EstadoPedido.CREADO, null);
        conCantidadCero.getDetalles().add(DetallePedido.builder().producto(s.producto).cantidad(0).build());
        bloquear(conCantidadCero);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(5L, admin));

        Pedido sinProducto = pedido(6L, EstadoPedido.CREADO, null);
        sinProducto.getDetalles().add(DetallePedido.builder().producto(null).cantidad(1).build());
        bloquear(sinProducto);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(6L, admin));

        Pedido cantidadNula = pedido(7L, EstadoPedido.CREADO, null);
        cantidadNula.getDetalles().add(DetallePedido.builder().producto(s.producto).cantidad(null).build());
        bloquear(cantidadNula);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(7L, admin));
        assertEquals(EstadoPedido.CREADO, conCantidadCero.getEstado());
    }

    @Test
    @DisplayName("Cancelar: sin item de stock, stock inconsistente o desbordado -> IllegalState")
    void cancelar_stockNoRestituible() {
        Stock s = stock(8);
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        p.getDetalles().add(DetallePedido.builder().producto(s.producto).cantidad(2).build());
        bloquear(p);

        s.item.setCantidad(-1);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(5L, admin));

        s.item.setCantidad(null);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(5L, admin));

        s.item.setCantidad(Integer.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(5L, admin));

        when(itemInventarioRepository.buscarParaActualizarStock(20L, 7L, 3L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(5L, admin));
    }

    @Test
    @DisplayName("Cancelar: item de stock de otro comercio o producto ajeno -> AccessDenied")
    void cancelar_stockDeOtroComercio() {
        Comercio otro = Comercio.builder().id(2L).build();
        Producto ajeno = producto(7L, "Ajeno", otro);
        Inventario inv = inventario(20L, comercio1);
        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inv);
        Pedido p = pedido(5L, EstadoPedido.CREADO, null);
        p.getDetalles().add(DetallePedido.builder().producto(ajeno).cantidad(1).build());
        bloquear(p);

        assertThrows(AccessDeniedException.class, () -> servicio.cancelar(5L, admin));

        Producto propio = producto(7L, "Propio", comercio1);
        Pedido q = pedido(6L, EstadoPedido.CREADO, null);
        q.getDetalles().add(DetallePedido.builder().producto(propio).cantidad(1).build());
        bloquear(q);
        when(itemInventarioRepository.buscarParaActualizarStock(20L, 7L, 3L))
                .thenReturn(Optional.of(item(inv, propio, deposito(3L, otro), 5)));
        assertThrows(AccessDeniedException.class, () -> servicio.cancelar(6L, admin));
        verify(itemInventarioRepository, never()).save(any());
    }

    // ═════════════════════════ helpers ═════════════════════════

    /** Inventario + producto + item de stock del comercio 1 / deposito 3, ya cableado en los mocks. */
    private Stock stock(int cantidad) {
        Producto prod = producto(7L, "Zapatillas", comercio1);
        Inventario inv = inventario(20L, comercio1);
        ItemInventario item = item(inv, prod, deposito3, cantidad);
        when(inventarioService.obtenerPorComercio(1L)).thenReturn(inv);
        when(productoRepository.findById(7L)).thenReturn(Optional.of(prod));
        when(itemInventarioRepository.buscarParaActualizarStock(20L, 7L, 3L)).thenReturn(Optional.of(item));
        return new Stock(prod, item);
    }

    private record Stock(Producto producto, ItemInventario item) { }

    private void bloquear(Pedido p) {
        when(pedidosRepository.findByIdForUpdate(p.getId())).thenReturn(Optional.of(p));
    }

    private void bloquear(Repartidor r) {
        when(repartidorRepository.findByIdForUpdate(r.getId())).thenReturn(Optional.of(r));
    }

    private CrearPedidoDTO dto(Long comercioId, Long depositoId, DetallePedidoDTO... detalles) {
        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(comercioId);
        dto.setDepositoId(depositoId);
        dto.setDireccionOrigen("Origen 1");
        dto.setDireccionDestino("Destino 2");
        if (detalles.length > 0) {
            dto.setDetalles(List.of(detalles));
        }
        return dto;
    }

    private DetallePedidoDTO detalle(Long productoId, Integer cantidad) {
        DetallePedidoDTO d = new DetallePedidoDTO();
        d.setProductoId(productoId);
        d.setCantidad(cantidad);
        return d;
    }

    private CotizacionDTO cotizacion(int objetos) {
        return new CotizacionDTO(5.0, 20, objetos, new BigDecimal("100"), new BigDecimal("50"),
                new BigDecimal("10"), new BigDecimal("160"), "ARS", false, false, "OSRM",
                new PuntoDTO("Origen cotizado", -34.60, -58.40), new PuntoDTO("Destino cotizado", -34.70, -58.50));
    }

    private Usuario usuario(Long id, Rol rol) {
        return Usuario.builder().id(id).rol(rol).estado(EstadoUsuario.VALIDADO)
                .nombre("N" + id).apellido("A").build();
    }

    private Usuario usuarioRepartidor(Rol rol, EstadoUsuario estado) {
        return Usuario.builder().id(1L).rol(rol).estado(estado).nombre("Carlos").apellido("Ruiz").build();
    }

    private Deposito deposito(Long id, Comercio comercio) {
        Deposito d = new Deposito();
        d.setId(id);
        d.setComercio(comercio);
        return d;
    }

    private Inventario inventario(Long id, Comercio comercio) {
        Inventario i = new Inventario();
        i.setId(id);
        i.setComercio(comercio);
        return i;
    }

    private Producto producto(Long id, String nombre, Comercio comercio) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setComercio(comercio);
        return p;
    }

    private ItemInventario item(Inventario inv, Producto prod, Deposito dep, Integer cantidad) {
        ItemInventario item = new ItemInventario();
        item.setId(50L);
        item.setInventario(inv);
        item.setProducto(prod);
        item.setDeposito(dep);
        item.setCantidad(cantidad);
        item.setStockMinimo(5);
        return item;
    }

    private Repartidor repartidor(Long id, EstadoRepartidor estado, boolean activo) {
        return Repartidor.builder().id(id).usuarioId(1L).patente("ABC123").zona("Palermo")
                .estado(estado).activo(activo).build();
    }

    private Pedido pedido(Long id, EstadoPedido estado, Repartidor repartidor) {
        return Pedido.builder().id(id).comercioId(1L).depositoId(3L).estado(estado).repartidor(repartidor)
                .direccionOrigen("Origen 1").direccionDestino("Destino 2").build();
    }
}
