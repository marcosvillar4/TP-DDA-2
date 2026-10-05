package com.example.DA2Back.service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.inventario.negocio.IInventario;
import com.example.DA2Back.inventario.negocio.ItemInventarioService;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.negocio.IProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemInventarioServiceTest {

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @Mock
    private IInventario inventarioService;

    @Mock
    private IProductoService productoService;

    @Mock
    private IDeposito depositoService;

    @InjectMocks
    private ItemInventarioService itemInventarioService;

    @Test
    @DisplayName("Crear item con inventario, producto y depósito del mismo comercio guarda el stock")
    void crear_mismoComercio_permitido() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto producto = producto(20L, comercio);
        Deposito deposito = deposito(30L, comercio);

        when(inventarioService.obtenerPorId(10L)).thenReturn(inventario);
        when(productoService.obtenerPorId(20L)).thenReturn(producto);
        when(depositoService.obtenerEntidadPorId(30L)).thenReturn(deposito);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 20L, 30L))
                .thenReturn(Optional.empty());
        when(itemInventarioRepository.save(any(ItemInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemInventario creado = itemInventarioService.crear(10L, 20L, 30L, 5);

        assertSame(inventario, creado.getInventario());
        assertSame(producto, creado.getProducto());
        assertSame(deposito, creado.getDeposito());
        assertEquals(5, creado.getCantidad());
        verify(itemInventarioRepository).save(creado);
    }

    @Test
    @DisplayName("Crear item rechaza producto de otro comercio")
    void crear_productoDeOtroComercio_rechazado() {
        Inventario inventario = inventario(10L, comercio(1L));
        Producto producto = producto(20L, comercio(2L));
        Deposito deposito = deposito(30L, comercio(1L));

        when(inventarioService.obtenerPorId(10L)).thenReturn(inventario);
        when(productoService.obtenerPorId(20L)).thenReturn(producto);
        when(depositoService.obtenerEntidadPorId(30L)).thenReturn(deposito);

        assertThrows(IllegalStateException.class,
                () -> itemInventarioService.crear(10L, 20L, 30L, 5));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear item rechaza depósito de otro comercio")
    void crear_depositoDeOtroComercio_rechazado() {
        Inventario inventario = inventario(10L, comercio(1L));
        Producto producto = producto(20L, comercio(1L));
        Deposito deposito = deposito(30L, comercio(2L));

        when(inventarioService.obtenerPorId(10L)).thenReturn(inventario);
        when(productoService.obtenerPorId(20L)).thenReturn(producto);
        when(depositoService.obtenerEntidadPorId(30L)).thenReturn(deposito);

        assertThrows(IllegalStateException.class,
                () -> itemInventarioService.crear(10L, 20L, 30L, 5));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear combinación existente suma cantidad sobre el item actual")
    void crear_combinacionExistente_sumaCantidad() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto producto = producto(20L, comercio);
        Deposito deposito = deposito(30L, comercio);
        ItemInventario existente = item(100L, inventario, producto, deposito, 5);

        when(inventarioService.obtenerPorId(10L)).thenReturn(inventario);
        when(productoService.obtenerPorId(20L)).thenReturn(producto);
        when(depositoService.obtenerEntidadPorId(30L)).thenReturn(deposito);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 20L, 30L))
                .thenReturn(Optional.of(existente));
        when(itemInventarioRepository.save(existente)).thenReturn(existente);

        ItemInventario actualizado = itemInventarioService.crear(10L, 20L, 30L, 3);

        assertSame(existente, actualizado);
        assertEquals(8, actualizado.getCantidad());
        verify(itemInventarioRepository).save(existente);
    }

    @Test
    @DisplayName("Editar item rechaza producto de otro comercio")
    void actualizar_productoDeOtroComercio_rechazado() {
        Comercio comercio = comercio(1L);
        ItemInventario item = item(100L, inventario(10L, comercio),
                producto(20L, comercio), deposito(30L, comercio), 5);
        Producto productoOtroComercio = producto(21L, comercio(2L));

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(productoService.obtenerPorId(21L)).thenReturn(productoOtroComercio);

        assertThrows(IllegalStateException.class,
                () -> itemInventarioService.actualizar(100L, 21L, null, 7));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Editar item rechaza depósito de otro comercio")
    void actualizar_depositoDeOtroComercio_rechazado() {
        Comercio comercio = comercio(1L);
        ItemInventario item = item(100L, inventario(10L, comercio),
                producto(20L, comercio), deposito(30L, comercio), 5);
        Deposito depositoOtroComercio = deposito(31L, comercio(2L));

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(depositoService.obtenerEntidadPorId(31L)).thenReturn(depositoOtroComercio);

        assertThrows(IllegalStateException.class,
                () -> itemInventarioService.actualizar(100L, null, 31L, 7));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Editar item rechaza combinación final existente en otro item")
    void actualizar_combinacionDuplicada_rechazado() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto productoOriginal = producto(20L, comercio);
        Producto productoFinal = producto(21L, comercio);
        Deposito depositoOriginal = deposito(30L, comercio);
        Deposito depositoFinal = deposito(31L, comercio);
        ItemInventario item = item(100L, inventario, productoOriginal, depositoOriginal, 5);
        ItemInventario otroItem = item(101L, inventario, productoFinal, depositoFinal, 9);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(productoService.obtenerPorId(21L)).thenReturn(productoFinal);
        when(depositoService.obtenerEntidadPorId(31L)).thenReturn(depositoFinal);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 21L, 31L))
                .thenReturn(Optional.of(otroItem));

        assertThrows(IllegalStateException.class,
                () -> itemInventarioService.actualizar(100L, 21L, 31L, 7));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Editar item permite cambiar producto por otro del mismo comercio")
    void actualizar_productoMismoComercio_permitido() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto productoOriginal = producto(20L, comercio);
        Producto productoFinal = producto(21L, comercio);
        Deposito deposito = deposito(30L, comercio);
        ItemInventario item = item(100L, inventario, productoOriginal, deposito, 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(productoService.obtenerPorId(21L)).thenReturn(productoFinal);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 21L, 30L))
                .thenReturn(Optional.empty());
        when(itemInventarioRepository.save(item)).thenReturn(item);

        ItemInventario actualizado = itemInventarioService.actualizar(100L, 21L, null, 7);

        assertSame(productoFinal, actualizado.getProducto());
        assertSame(deposito, actualizado.getDeposito());
        assertEquals(7, actualizado.getCantidad());
    }

    @Test
    @DisplayName("Editar item permite cambiar depósito por otro del mismo comercio")
    void actualizar_depositoMismoComercio_permitido() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto producto = producto(20L, comercio);
        Deposito depositoOriginal = deposito(30L, comercio);
        Deposito depositoFinal = deposito(31L, comercio);
        ItemInventario item = item(100L, inventario, producto, depositoOriginal, 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(depositoService.obtenerEntidadPorId(31L)).thenReturn(depositoFinal);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 20L, 31L))
                .thenReturn(Optional.empty());
        when(itemInventarioRepository.save(item)).thenReturn(item);

        ItemInventario actualizado = itemInventarioService.actualizar(100L, null, 31L, 7);

        assertSame(producto, actualizado.getProducto());
        assertSame(depositoFinal, actualizado.getDeposito());
        assertEquals(7, actualizado.getCantidad());
    }

    @Test
    @DisplayName("Crear item permite cantidad cero")
    void crear_cantidadCero_permitida() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto producto = producto(20L, comercio);
        Deposito deposito = deposito(30L, comercio);

        when(inventarioService.obtenerPorId(10L)).thenReturn(inventario);
        when(productoService.obtenerPorId(20L)).thenReturn(producto);
        when(depositoService.obtenerEntidadPorId(30L)).thenReturn(deposito);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 20L, 30L))
                .thenReturn(Optional.empty());
        when(itemInventarioRepository.save(any(ItemInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemInventario creado = itemInventarioService.crear(10L, 20L, 30L, 0);

        assertEquals(0, creado.getCantidad());
        verify(itemInventarioRepository).save(creado);
    }

    @Test
    @DisplayName("Actualizar cantidad permite cero")
    void actualizarCantidad_cero_permitido() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(itemInventarioRepository.save(item)).thenReturn(item);

        ItemInventario actualizado = itemInventarioService.actualizarCantidad(100L, 0);

        assertEquals(0, actualizado.getCantidad());
        verify(itemInventarioRepository).save(item);
    }

    @Test
    @DisplayName("Actualizar cantidad rechaza negativos")
    void actualizarCantidad_negativa_rechazada() {
        assertThrows(IllegalArgumentException.class,
                () -> itemInventarioService.actualizarCantidad(100L, -1));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar cantidad rechaza null")
    void actualizarCantidad_null_rechazada() {
        assertThrows(IllegalArgumentException.class,
                () -> itemInventarioService.actualizarCantidad(100L, null));

        verify(itemInventarioRepository, never()).save(any());
    }

    private Comercio comercio(Long id) {
        Comercio comercio = new Comercio();
        comercio.setId(id);
        return comercio;
    }

    private Inventario inventario(Long id, Comercio comercio) {
        Inventario inventario = new Inventario();
        inventario.setId(id);
        inventario.setComercio(comercio);
        return inventario;
    }

    private Producto producto(Long id, Comercio comercio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setComercio(comercio);
        return producto;
    }

    private Deposito deposito(Long id, Comercio comercio) {
        Deposito deposito = new Deposito();
        deposito.setId(id);
        deposito.setComercio(comercio);
        return deposito;
    }

    private ItemInventario item(
            Long id,
            Inventario inventario,
            Producto producto,
            Deposito deposito,
            Integer cantidad) {

        ItemInventario item = new ItemInventario();
        item.setId(id);
        item.setInventario(inventario);
        item.setProducto(producto);
        item.setDeposito(deposito);
        item.setCantidad(cantidad);
        return item;
    }
}
