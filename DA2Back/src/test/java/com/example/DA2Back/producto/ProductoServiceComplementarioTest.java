package com.example.DA2Back.producto;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.producto.negocio.ProductoService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Validaciones de entrada, normalizacion de texto y deteccion de SKU duplicado de ProductoService. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductoServiceComplementarioTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private IComercio comercioService;

    private ProductoService service;
    private final Comercio comercio = comercio(1L);

    @BeforeEach
    void setUp() {
        service = new ProductoService(productoRepository, comercioService);
        when(comercioService.obtenerEntidadPorId(1L)).thenReturn(comercio);
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ───────────── lecturas ─────────────

    @Test
    @DisplayName("obtenerPorId: nulo -> IllegalArgument; inexistente -> NoSuchElement; ok")
    void obtenerPorId() {
        assertEquals("El ID del producto no puede ser null",
                assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(null)).getMessage());
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals("No se encontró el producto con ID: 9",
                assertThrows(NoSuchElementException.class, () -> service.obtenerPorId(9L)).getMessage());

        Producto p = producto(5L, "SKU1", "Nombre", "Cat");
        when(productoRepository.findById(5L)).thenReturn(Optional.of(p));
        assertSame(p, service.obtenerPorId(5L));
    }

    @Test
    @DisplayName("obtenerTodos y obtenerPorComercio delegan; comercio nulo o inexistente se rechaza")
    void listados() {
        Producto p = producto(5L, "SKU1", "Nombre", "Cat");
        when(productoRepository.findAll()).thenReturn(List.of(p));
        when(productoRepository.findByComercioId(1L)).thenReturn(List.of(p));
        assertEquals(1, service.obtenerTodos().size());
        assertEquals(1, service.obtenerPorComercio(1L).size());

        assertEquals("El ID del comercio es obligatorio",
                assertThrows(IllegalArgumentException.class, () -> service.obtenerPorComercio(null)).getMessage());

        when(comercioService.obtenerEntidadPorId(404L)).thenThrow(new RuntimeException("no existe"));
        assertEquals("No se encontró el comercio con ID: 404",
                assertThrows(NoSuchElementException.class, () -> service.obtenerPorComercio(404L)).getMessage());
        verify(productoRepository, never()).findByComercioId(404L);
    }

    @Test
    @DisplayName("buscar normaliza los filtros de texto: blancos -> null y recorta espacios; sin comercio no lo valida")
    void buscar_normalizaFiltros() {
        service.buscar(null, null, "   ", "  ");
        verify(productoRepository).buscar(null, null, null, null);

        service.buscar(1L, EstadoProducto.ACTIVO, "  Calzado ", " zapa ");
        verify(productoRepository).buscar(1L, EstadoProducto.ACTIVO, "Calzado", "zapa");

        service.buscar(null, null, null, null);
        verify(comercioService, never()).obtenerEntidadPorId(null);
    }

    // ───────────── crear ─────────────

    @Test
    @DisplayName("Crear: producto nulo se rechaza")
    void crear_nulo() {
        assertEquals("El producto no puede ser null",
                assertThrows(IllegalArgumentException.class, () -> service.crear(null)).getMessage());
    }

    @Test
    @DisplayName("Crear: SKU, nombre y categoria obligatorios (en blanco se rechazan con su mensaje)")
    void crear_camposObligatorios() {
        assertEquals("El SKU es obligatorio", assertThrows(IllegalArgumentException.class,
                () -> service.crear(producto(null, "  ", "N", "C"))).getMessage());
        assertEquals("El nombre del producto es obligatorio", assertThrows(IllegalArgumentException.class,
                () -> service.crear(producto(null, "S", "   ", "C"))).getMessage());
        assertEquals("La categoría del producto es obligatoria", assertThrows(IllegalArgumentException.class,
                () -> service.crear(producto(null, "S", "N", null))).getMessage());
        assertEquals("El nombre del producto es obligatorio", assertThrows(IllegalArgumentException.class,
                () -> service.crear(producto(null, "S", null, "C"))).getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear normaliza (recorta) SKU, nombre, categoria y descripcion; descripcion en blanco queda null")
    void crear_normaliza() {
        Producto p = producto(null, "  SKU-9 ", " Zapatilla ", " Calzado ");
        p.setDescripcion("   ");

        Producto creado = service.crear(p);

        assertEquals("SKU-9", creado.getSku());
        assertEquals("Zapatilla", creado.getNombre());
        assertEquals("Calzado", creado.getCategoria());
        assertNull(creado.getDescripcion());
        assertEquals(EstadoProducto.ACTIVO, creado.getEstado());
        assertSame(comercio, creado.getComercio());
    }

    @Test
    @DisplayName("Crear: comercio sin id -> IllegalArgument; comercio inexistente -> NoSuchElement")
    void crear_comercioInvalido() {
        Producto sinId = producto(null, "S", "N", "C");
        sinId.setComercio(new Comercio());
        assertEquals("El ID del comercio es obligatorio",
                assertThrows(IllegalArgumentException.class, () -> service.crear(sinId)).getMessage());

        Producto inexistente = producto(null, "S", "N", "C");
        inexistente.setComercio(comercio(404L));
        when(comercioService.obtenerEntidadPorId(404L)).thenThrow(new RuntimeException("no existe"));
        assertThrows(NoSuchElementException.class, () -> service.crear(inexistente));
    }

    @Test
    @DisplayName("Crear con SKU ya usado en el comercio (sin distinguir mayusculas) -> IllegalState")
    void crear_skuDuplicado() {
        when(productoRepository.findByComercioIdAndSkuIgnoreCase(1L, "SKU1"))
                .thenReturn(Optional.of(producto(77L, "sku1", "Otro", "Cat")));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crear(producto(null, "SKU1", "N", "C")));

        assertEquals("Ya existe un producto con SKU SKU1 para el comercio indicado", ex.getMessage());
        verify(productoRepository, never()).save(any());
    }

    // ───────────── violaciones de integridad ─────────────

    @Test
    @DisplayName("Violacion de la constraint uk_producto_comercio_sku (por nombre de constraint) -> mensaje de SKU duplicado")
    void guardar_constraintPorNombre() {
        ConstraintViolationException cve = new ConstraintViolationException("dup", new SQLException("x"),
                "UK_PRODUCTO_COMERCIO_SKU");
        when(productoRepository.save(any(Producto.class))).thenThrow(new DataIntegrityViolationException("x", cve));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crear(producto(null, "S", "N", "C")));

        assertEquals("Ya existe un producto con ese SKU para el comercio indicado", ex.getMessage());
    }

    @Test
    @DisplayName("Violacion detectada por el mensaje de una causa anidada tambien se traduce")
    void guardar_constraintPorMensajeDeCausa() {
        DataIntegrityViolationException dive = new DataIntegrityViolationException("generico",
                new RuntimeException("Duplicate entry for key 'uk_producto_comercio_sku'"));
        when(productoRepository.save(any(Producto.class))).thenThrow(dive);

        assertThrows(IllegalStateException.class, () -> service.crear(producto(null, "S", "N", "C")));
    }

    @Test
    @DisplayName("Otra violacion de integridad (no del SKU) se relanza tal cual, sin disfrazarla")
    void guardar_otraViolacion_seRelanza() {
        DataIntegrityViolationException otra = new DataIntegrityViolationException("not null violation",
                new RuntimeException("Column 'nombre' cannot be null"));
        when(productoRepository.save(any(Producto.class))).thenThrow(otra);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> service.crear(producto(null, "S", "N", "C")));

        assertSame(otra, ex);
    }

    // ───────────── actualizar ─────────────

    @Test
    @DisplayName("Actualizar: id o producto nulos se rechazan; inexistente -> NoSuchElement")
    void actualizar_invalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(null, producto(null, "S", "N", "C")));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, null));
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.actualizar(9L, producto(null, "S", "N", "C")));
    }

    @Test
    @DisplayName("Actualizar con nombre o categoria en blanco se rechaza y no modifica el producto")
    void actualizar_camposEnBlanco() {
        Producto existente = producto(5L, "SKU1", "Original", "Cat");
        when(productoRepository.findById(5L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5L, producto(null, "X", "  ", "C")));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5L, producto(null, "X", "N", "")));

        assertEquals("Original", existente.getNombre());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar cambia solo nombre, descripcion y categoria (el SKU y el estado no se tocan)")
    void actualizar_noTocaSkuNiEstado() {
        Producto existente = producto(5L, "SKU1", "Original", "Cat");
        existente.setEstado(EstadoProducto.INACTIVO);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(existente));
        Producto cambios = producto(null, "OTRO-SKU", " Nuevo ", " Nueva cat ");
        cambios.setDescripcion(" desc ");

        Producto res = service.actualizar(5L, cambios);

        assertEquals("SKU1", res.getSku());
        assertEquals("Nuevo", res.getNombre());
        assertEquals("Nueva cat", res.getCategoria());
        assertEquals("desc", res.getDescripcion());
        assertEquals(EstadoProducto.INACTIVO, res.getEstado());
    }

    // ───────────── activar / desactivar ─────────────

    @Test
    @DisplayName("Activar y desactivar inexistente -> NoSuchElement; id nulo -> IllegalArgument")
    void activarDesactivar_errores() {
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.activar(9L));
        assertThrows(NoSuchElementException.class, () -> service.desactivar(9L));
        assertThrows(IllegalArgumentException.class, () -> service.activar(null));
    }

    // ───────────── helpers ─────────────

    private Comercio comercio(Long id) {
        Comercio c = new Comercio();
        c.setId(id);
        return c;
    }

    private Producto producto(Long id, String sku, String nombre, String categoria) {
        Producto p = new Producto();
        p.setId(id);
        p.setSku(sku);
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setComercio(comercio);
        return p;
    }
}
