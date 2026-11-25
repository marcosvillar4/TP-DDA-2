package com.example.DA2Back.inventario;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.inventario.negocio.IInventario;
import com.example.DA2Back.inventario.negocio.ItemInventarioService;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.negocio.IProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Complementa ItemInventarioServiceTest: validaciones de entrada, permisos por rol sobre depositos,
 * lecturas simples y la notificacion de stock minimo (que el test original no inyecta).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemInventarioServiceComplementarioTest {

    @Mock private ItemInventarioRepository repo;
    @Mock private IInventario inventarioService;
    @Mock private IProductoService productoService;
    @Mock private IDeposito depositoService;
    @Mock private IComercio comercioService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private ItemInventarioService service;

    // ───────────── lecturas ─────────────

    @Test
    @DisplayName("obtenerPorId: nulo -> IllegalArgument; inexistente -> NoSuchElement con el id; ok")
    void obtenerPorId() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(null));

        when(repo.findById(9L)).thenReturn(Optional.empty());
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> service.obtenerPorId(9L));
        assertEquals("No se encontró el item de inventario con ID: 9", ex.getMessage());

        ItemInventario item = item(1L, 10, 5);
        when(repo.findById(1L)).thenReturn(Optional.of(item));
        assertSame(item, service.obtenerPorId(1L));
    }

    @Test
    @DisplayName("Consultas por inventario, deposito y producto: id nulo se rechaza y id valido delega")
    void consultasPorFiltro() {
        ItemInventario item = item(1L, 10, 5);
        when(repo.findAll()).thenReturn(List.of(item));
        when(repo.findByInventarioId(1L)).thenReturn(List.of(item));
        when(repo.findByDepositoId(2L)).thenReturn(List.of(item));
        when(repo.findByProductoId(3L)).thenReturn(List.of(item));

        assertEquals(1, service.obtenerTodos().size());
        assertEquals(1, service.obtenerPorInventario(1L).size());
        assertEquals(1, service.obtenerPorDeposito(2L).size());
        assertEquals(1, service.obtenerPorProducto(3L).size());

        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorInventario(null));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorDeposito(null));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorProducto(null));
    }

    // ───────────── permisos sobre depositos ─────────────

    @Test
    @DisplayName("Ver inventario de un deposito: usuario invalido (null, sin id, sin rol) -> AccessDenied")
    void obtenerPorDeposito_usuarioInvalido() {
        assertThrows(AccessDeniedException.class, () -> service.obtenerPorDeposito(1L, null));
        assertThrows(AccessDeniedException.class,
                () -> service.obtenerPorDeposito(1L, Usuario.builder().rol(Rol.ADMIN).build()));
        assertThrows(AccessDeniedException.class,
                () -> service.obtenerPorDeposito(1L, Usuario.builder().id(1L).build()));
    }

    @Test
    @DisplayName("Ver inventario de un deposito: deposito nulo -> IllegalArgument")
    void obtenerPorDeposito_depositoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerPorDeposito(null, usuario(1L, Rol.ADMIN)));
    }

    @Test
    @DisplayName("ADMIN ve cualquier deposito sin consultar su detalle")
    void obtenerPorDeposito_admin() {
        when(repo.findByDepositoId(7L)).thenReturn(List.of(item(1L, 3, 5)));

        assertEquals(1, service.obtenerPorDeposito(7L, usuario(1L, Rol.ADMIN)).size());
        verify(depositoService, never()).obtenerPorId(any());
    }

    @Test
    @DisplayName("DEPOSITO ve solo el deposito que tiene asignado")
    void obtenerPorDeposito_depositoPropioYAjeno() {
        when(depositoService.obtenerPorId(7L)).thenReturn(DepositoResponseDTO.builder().id(7L).usuarioId(50L).build());
        when(repo.findByDepositoId(7L)).thenReturn(List.of(item(1L, 3, 5)));

        assertEquals(1, service.obtenerPorDeposito(7L, usuario(50L, Rol.DEPOSITO)).size());
        assertThrows(AccessDeniedException.class, () -> service.obtenerPorDeposito(7L, usuario(51L, Rol.DEPOSITO)));
    }

    @Test
    @DisplayName("COMERCIO ve solo depositos de su comercio; REPARTIDOR nunca")
    void obtenerPorDeposito_comercio() {
        when(depositoService.obtenerPorId(7L)).thenReturn(DepositoResponseDTO.builder().id(7L).comercioId(1L).usuarioId(50L).build());
        when(comercioService.obtenerPorUsuarioId(60L)).thenReturn(ComercioResponseDTO.builder().id(1L).build());
        when(comercioService.obtenerPorUsuarioId(61L)).thenReturn(ComercioResponseDTO.builder().id(2L).build());
        when(repo.findByDepositoId(7L)).thenReturn(List.of(item(1L, 3, 5)));

        assertEquals(1, service.obtenerPorDeposito(7L, usuario(60L, Rol.COMERCIO)).size());
        assertThrows(AccessDeniedException.class, () -> service.obtenerPorDeposito(7L, usuario(61L, Rol.COMERCIO)));
        assertThrows(AccessDeniedException.class, () -> service.obtenerPorDeposito(7L, usuario(70L, Rol.REPARTIDOR)));
    }

    // ───────────── crear ─────────────

    @Test
    @DisplayName("Crear: cada argumento obligatorio nulo se rechaza con su propio mensaje")
    void crear_argumentosNulos() {
        assertEquals("El ID del inventario no puede ser nulo",
                assertThrows(IllegalArgumentException.class, () -> service.crear(null, 2L, 3L, 1)).getMessage());
        assertEquals("El ID del producto no puede ser nulo",
                assertThrows(IllegalArgumentException.class, () -> service.crear(1L, null, 3L, 1)).getMessage());
        assertEquals("El ID del depósito no puede ser nulo",
                assertThrows(IllegalArgumentException.class, () -> service.crear(1L, 2L, null, 1)).getMessage());
        assertEquals("La cantidad no puede ser nula",
                assertThrows(IllegalArgumentException.class, () -> service.crear(1L, 2L, 3L, null)).getMessage());
        assertEquals("La cantidad no puede ser negativa",
                assertThrows(IllegalArgumentException.class, () -> service.crear(1L, 2L, 3L, -1)).getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("Crear: inventario sin comercio asociado -> IllegalState")
    void crear_inventarioSinComercio() {
        when(inventarioService.obtenerPorId(1L)).thenReturn(inventario(1L, null));
        when(productoService.obtenerPorId(2L)).thenReturn(producto(2L, comercio(1L)));
        when(depositoService.obtenerEntidadPorId(3L)).thenReturn(deposito(3L, comercio(1L)));

        assertEquals("El inventario no tiene un comercio asociado",
                assertThrows(IllegalStateException.class, () -> service.crear(1L, 2L, 3L, 1)).getMessage());
    }

    @Test
    @DisplayName("Crear con stock minimo explicito y distinto de 5 lo conserva en el item nuevo")
    void crear_stockMinimoExplicito() {
        prepararCreacion();
        when(repo.findByInventarioIdAndProductoIdAndDepositoId(1L, 2L, 3L)).thenReturn(Optional.empty());
        when(repo.save(any(ItemInventario.class))).thenAnswer(i -> i.getArgument(0));

        ItemInventario creado = service.crear(1L, 2L, 3L, 10, 8);

        assertEquals(8, creado.getStockMinimo());
        assertEquals(10, creado.getCantidad());
    }

    @Test
    @DisplayName("Crear sin stock minimo usa el valor por defecto (5)")
    void crear_stockMinimoPorDefecto() {
        prepararCreacion();
        when(repo.findByInventarioIdAndProductoIdAndDepositoId(1L, 2L, 3L)).thenReturn(Optional.empty());
        when(repo.save(any(ItemInventario.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(5, service.crear(1L, 2L, 3L, 1, null).getStockMinimo());
        assertEquals(5, service.crear(1L, 2L, 3L, 1).getStockMinimo());
    }

    // ───────────── actualizarCantidad ─────────────

    @Test
    @DisplayName("actualizarCantidad con id nulo se rechaza")
    void actualizarCantidad_idNulo() {
        assertEquals("El ID del item de inventario no puede ser nulo",
                assertThrows(IllegalArgumentException.class, () -> service.actualizarCantidad(null, 1)).getMessage());
    }

    @Test
    @DisplayName("actualizarCantidad guarda y avisa a Notificaciones con la cantidad anterior y la nueva")
    void actualizarCantidad_notificaCruce() {
        ItemInventario item = item(1L, 6, 5);
        when(repo.findById(1L)).thenReturn(Optional.of(item));
        when(repo.save(item)).thenReturn(item);

        ItemInventario res = service.actualizarCantidad(1L, 2);

        assertEquals(2, res.getCantidad());
        verify(notificacionService).notificarSiCruzoStockMinimo(item, 6, 2);
    }

    // ───────────── actualizar ─────────────

    @Test
    @DisplayName("actualizar: cantidad nula o negativa se rechaza antes de consultar nada")
    void actualizar_cantidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, null, null, -3));
        verify(repo, never()).findById(any());
    }

    @Test
    @DisplayName("actualizar sin producto ni deposito nuevos conserva los actuales y notifica el cambio de stock")
    void actualizar_conservaProductoYDeposito() {
        Comercio comercio = comercio(1L);
        Producto producto = producto(2L, comercio);
        Deposito deposito = deposito(3L, comercio);
        ItemInventario item = item(1L, 9, 5);
        item.setInventario(inventario(10L, comercio));
        item.setProducto(producto);
        item.setDeposito(deposito);
        when(repo.findById(1L)).thenReturn(Optional.of(item));
        when(repo.findByInventarioIdAndProductoIdAndDepositoId(10L, 2L, 3L)).thenReturn(Optional.of(item));
        when(repo.save(item)).thenReturn(item);

        ItemInventario res = service.actualizar(1L, null, null, 4);

        assertSame(producto, res.getProducto());
        assertSame(deposito, res.getDeposito());
        assertEquals(4, res.getCantidad());
        verify(productoService, never()).obtenerPorId(any());
        verify(notificacionService).notificarSiCruzoStockMinimo(item, 9, 4);
    }

    // ───────────── actualizarStockMinimo ─────────────

    @Test
    @DisplayName("actualizarStockMinimo: usuario invalido -> AccessDenied; ADMIN puede en cualquier item")
    void actualizarStockMinimo_usuarioYAdmin() {
        assertThrows(AccessDeniedException.class, () -> service.actualizarStockMinimo(1L, 3, null));

        ItemInventario item = item(1L, 9, 5);
        item.setInventario(inventario(10L, comercio(1L)));
        when(repo.findById(1L)).thenReturn(Optional.of(item));
        when(repo.save(item)).thenReturn(item);

        assertEquals(7, service.actualizarStockMinimo(1L, 7, usuario(1L, Rol.ADMIN)).getStockMinimo());
    }

    // ───────────── alertas ─────────────

    @Test
    @DisplayName("obtenerAlertasStock: usuario nulo -> AccessDenied")
    void alertas_usuarioNulo() {
        assertThrows(AccessDeniedException.class, () -> service.obtenerAlertasStock(null));
    }

    // ───────────── eliminar ─────────────

    @Test
    @DisplayName("eliminar: id nulo, inexistente (no borra) y ok")
    void eliminar() {
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(null));

        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.eliminar(9L));
        verify(repo, never()).delete(any());

        ItemInventario item = item(1L, 1, 5);
        when(repo.findById(1L)).thenReturn(Optional.of(item));
        service.eliminar(1L);
        verify(repo).delete(item);
    }

    // ───────────── helpers ─────────────

    private void prepararCreacion() {
        Comercio comercio = comercio(1L);
        when(inventarioService.obtenerPorId(1L)).thenReturn(inventario(1L, comercio));
        when(productoService.obtenerPorId(2L)).thenReturn(producto(2L, comercio));
        when(depositoService.obtenerEntidadPorId(3L)).thenReturn(deposito(3L, comercio));
    }

    private Comercio comercio(Long id) {
        Comercio c = new Comercio();
        c.setId(id);
        return c;
    }

    private Inventario inventario(Long id, Comercio comercio) {
        Inventario i = new Inventario();
        i.setId(id);
        i.setComercio(comercio);
        return i;
    }

    private Producto producto(Long id, Comercio comercio) {
        Producto p = new Producto();
        p.setId(id);
        p.setComercio(comercio);
        return p;
    }

    private Deposito deposito(Long id, Comercio comercio) {
        Deposito d = new Deposito();
        d.setId(id);
        d.setComercio(comercio);
        return d;
    }

    private ItemInventario item(Long id, int cantidad, int stockMinimo) {
        ItemInventario item = new ItemInventario();
        item.setId(id);
        item.setCantidad(cantidad);
        item.setStockMinimo(stockMinimo);
        return item;
    }

    private Usuario usuario(Long id, Rol rol) {
        return Usuario.builder().id(id).rol(rol).estado(EstadoUsuario.VALIDADO)
                .email("u" + id + "@mail.com").nombre("N").apellido("A").build();
    }
}
