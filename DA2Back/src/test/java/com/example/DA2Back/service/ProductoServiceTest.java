package com.example.DA2Back.service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.producto.negocio.ProductoService;
import com.example.DA2Back.producto.presentacion.ProductoController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private IComercio comercioService;

    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("Crear producto asigna ACTIVO por defecto, normaliza campos y resuelve comercio")
    void crearProducto_valido_guardaActivoPorDefecto() {

        Comercio comercio = comercio(1L, "Urban Shoes");
        Producto producto = producto(null, " US-001 ", " Zapatillas ", " Calzado deportivo ", " Calzado ", null, comercio);

        when(comercioService.obtenerPorId(1L)).thenReturn(comercio);
        when(productoRepository.findByComercioIdAndSkuIgnoreCase(1L, "US-001"))
                .thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto creado = productoService.crear(producto);

        assertEquals("US-001", creado.getSku());
        assertEquals("Zapatillas", creado.getNombre());
        assertEquals("Calzado deportivo", creado.getDescripcion());
        assertEquals("Calzado", creado.getCategoria());
        assertEquals(EstadoProducto.ACTIVO, creado.getEstado());
        assertSame(comercio, creado.getComercio());
        verify(productoRepository).save(creado);
    }

    @Test
    @DisplayName("Crear producto rechaza SKU duplicado dentro del mismo comercio")
    void crearProducto_skuDuplicadoMismoComercio_lanzaError() {

        Comercio comercio = comercio(1L, "Urban Shoes");
        Producto existente = producto(10L, "US-001", "Zapatillas", null, "Calzado", EstadoProducto.ACTIVO, comercio);
        Producto nuevo = producto(null, "US-001", "Otra zapatilla", null, "Calzado", null, comercio);

        when(comercioService.obtenerPorId(1L)).thenReturn(comercio);
        when(productoRepository.findByComercioIdAndSkuIgnoreCase(1L, "US-001"))
                .thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> productoService.crear(nuevo));
    }

    @Test
    @DisplayName("Crear producto permite repetir SKU si pertenece a otro comercio")
    void crearProducto_mismoSkuOtroComercio_permitido() {

        Comercio comercio = comercio(2L, "TecnoStore");
        Producto producto = producto(null, "US-001", "Auriculares", null, "Electrónica", null, comercio);

        when(comercioService.obtenerPorId(2L)).thenReturn(comercio);
        when(productoRepository.findByComercioIdAndSkuIgnoreCase(2L, "US-001"))
                .thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto creado = productoService.crear(producto);

        assertEquals("US-001", creado.getSku());
        assertSame(comercio, creado.getComercio());
        verify(productoRepository).save(creado);
    }

    @Test
    @DisplayName("Desactivar producto cambia estado a INACTIVO sin eliminarlo")
    void desactivarProducto_cambiaEstado() {

        Comercio comercio = comercio(1L, "Urban Shoes");
        Producto producto = producto(10L, "US-001", "Zapatillas", null, "Calzado", EstadoProducto.ACTIVO, comercio);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto desactivado = productoService.desactivar(10L);

        assertEquals(EstadoProducto.INACTIVO, desactivado.getEstado());
        verify(productoRepository).save(producto);
        verify(productoRepository, never()).delete(any(Producto.class));
    }

    @Test
    @DisplayName("Activar producto cambia estado a ACTIVO")
    void activarProducto_cambiaEstado() {

        Comercio comercio = comercio(1L, "Urban Shoes");
        Producto producto = producto(10L, "US-001", "Zapatillas", null, "Calzado", EstadoProducto.INACTIVO, comercio);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto activado = productoService.activar(10L);

        assertEquals(EstadoProducto.ACTIVO, activado.getEstado());
        verify(productoRepository).save(producto);
        verify(productoRepository, never()).delete(any(Producto.class));
    }

    @Test
    @DisplayName("Buscar delega filtros simples al repository")
    void buscarProductos_delegaFiltros() {

        Comercio comercio = comercio(1L, "Urban Shoes");

        when(comercioService.obtenerPorId(1L)).thenReturn(comercio);
        when(productoRepository.buscar(1L, EstadoProducto.ACTIVO, "Calzado", "US"))
                .thenReturn(List.of());

        productoService.buscar(1L, EstadoProducto.ACTIVO, " Calzado ", " US ");

        verify(productoRepository).buscar(1L, EstadoProducto.ACTIVO, "Calzado", "US");
    }

    @Test
    @DisplayName("Crear producto requiere comercio")
    void crearProducto_sinComercio_lanzaError() {

        Producto producto = producto(null, "US-001", "Zapatillas", null, "Calzado", null, null);

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
    }

    @Test
    @DisplayName("PUT modifica nombre, descripción y categoría, pero conserva SKU, comercio y estado")
    void actualizarProducto_conservaIdentidadYEstado() {

        Comercio comercio = comercio(1L, "Urban Shoes");
        Comercio otroComercio = comercio(2L, "TecnoStore");
        Producto existente = producto(10L, "US-001", "Zapatillas", null, "Calzado", EstadoProducto.ACTIVO, comercio);
        Producto cambios = producto(null, "SKU-MALICIOSO", " Zapatillas Pro ", " Nueva descripción ", " Indumentaria ", EstadoProducto.INACTIVO, otroComercio);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto actualizado = productoService.actualizar(10L, cambios);

        assertEquals("Zapatillas Pro", actualizado.getNombre());
        assertEquals("Nueva descripción", actualizado.getDescripcion());
        assertEquals("Indumentaria", actualizado.getCategoria());
        assertEquals("US-001", actualizado.getSku());
        assertSame(comercio, actualizado.getComercio());
        assertEquals(EstadoProducto.ACTIVO, actualizado.getEstado());

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getId());
        assertEquals("US-001", captor.getValue().getSku());
        assertSame(comercio, captor.getValue().getComercio());
        assertEquals(EstadoProducto.ACTIVO, captor.getValue().getEstado());
    }

    @Test
    @DisplayName("ProductoController no expone DELETE ni PATCH genérico de estado")
    void productoController_noExponeEndpointsRedundantes() {

        boolean exponeDelete = Arrays.stream(ProductoController.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(DeleteMapping.class));

        boolean exponePatchEstado = Arrays.stream(ProductoController.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(PatchMapping.class))
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .anyMatch(path -> path.contains("estado"));

        assertFalse(exponeDelete);
        assertFalse(exponePatchEstado);
    }

    private Comercio comercio(Long id, String nombre) {

        Comercio comercio = new Comercio();
        comercio.setId(id);
        comercio.setNombre(nombre);
        comercio.setDireccion("Av. Corrientes 1234");
        comercio.setTelefono("1122334455");
        comercio.setEmail("contacto@test.com");

        return comercio;
    }

    private Producto producto(
            Long id,
            String sku,
            String nombre,
            String descripcion,
            String categoria,
            EstadoProducto estado,
            Comercio comercio) {

        Producto producto = new Producto();
        producto.setId(id);
        producto.setSku(sku);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setEstado(estado);
        producto.setComercio(comercio);

        return producto;
    }
}
