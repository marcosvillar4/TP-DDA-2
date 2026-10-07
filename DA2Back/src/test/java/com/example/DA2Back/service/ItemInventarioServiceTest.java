package com.example.DA2Back.service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.inventario.negocio.IInventario;
import com.example.DA2Back.inventario.negocio.ItemInventarioService;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.negocio.IProductoService;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Mock
    private IComercio comercioService;

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
        assertEquals(5, creado.getStockMinimo());
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
    @DisplayName("Crear combinación existente conserva el stock mínimo actual")
    void crear_combinacionExistente_conservaStockMinimoActual() {
        Comercio comercio = comercio(1L);
        Inventario inventario = inventario(10L, comercio);
        Producto producto = producto(20L, comercio);
        Deposito deposito = deposito(30L, comercio);
        ItemInventario existente = item(100L, inventario, producto, deposito, 10);
        existente.setStockMinimo(5);

        when(inventarioService.obtenerPorId(10L)).thenReturn(inventario);
        when(productoService.obtenerPorId(20L)).thenReturn(producto);
        when(depositoService.obtenerEntidadPorId(30L)).thenReturn(deposito);
        when(itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(10L, 20L, 30L))
                .thenReturn(Optional.of(existente));
        when(itemInventarioRepository.save(existente)).thenReturn(existente);

        ItemInventario actualizado = itemInventarioService.crear(10L, 20L, 30L, 3, 20);

        assertSame(existente, actualizado);
        assertEquals(13, actualizado.getCantidad());
        assertEquals(5, actualizado.getStockMinimo());
        verify(itemInventarioRepository).save(existente);
    }

    @Test
    @DisplayName("Crear item con stock mínimo válido conserva el umbral indicado")
    void crear_stockMinimoValido_conservaValor() {
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

        ItemInventario creado = itemInventarioService.crear(10L, 20L, 30L, 7, 2);

        assertEquals(2, creado.getStockMinimo());
    }

    @Test
    @DisplayName("Crear item con stock mínimo negativo es rechazado")
    void crear_stockMinimoNegativo_rechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> itemInventarioService.crear(10L, 20L, 30L, 7, -1));

        verify(itemInventarioRepository, never()).save(any());
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

    @Test
    @DisplayName("Item con cantidad mayor al stock mínimo no aparece como stock bajo")
    void alertasStock_cantidadMayorAlMinimo_noAparece() {
        when(itemInventarioRepository.findAlertasStock()).thenReturn(List.of());

        List<ItemInventario> alertas = itemInventarioService.obtenerAlertasStock(usuario(1L, Rol.ADMIN));

        assertEquals(0, alertas.size());
    }

    @Test
    @DisplayName("Item con cantidad igual al stock mínimo aparece como stock bajo")
    void alertasStock_cantidadIgualAlMinimo_aparece() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 5);
        item.setStockMinimo(5);

        when(itemInventarioRepository.findAlertasStock()).thenReturn(List.of(item));

        List<ItemInventario> alertas = itemInventarioService.obtenerAlertasStock(usuario(1L, Rol.ADMIN));

        assertEquals(1, alertas.size());
        assertEquals(100L, alertas.get(0).getId());
    }

    @Test
    @DisplayName("Item con cantidad cero aparece como stock bajo")
    void alertasStock_cantidadCero_aparece() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 0);
        item.setStockMinimo(5);

        when(itemInventarioRepository.findAlertasStock()).thenReturn(List.of(item));

        List<ItemInventario> alertas = itemInventarioService.obtenerAlertasStock(usuario(1L, Rol.ADMIN));

        assertEquals(1, alertas.size());
        assertEquals(0, alertas.get(0).getCantidad());
    }

    @Test
    @DisplayName("Configurar stock mínimo negativo es rechazado")
    void actualizarStockMinimo_negativo_rechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> itemInventarioService.actualizarStockMinimo(100L, -1, usuario(1L, Rol.ADMIN)));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Configurar stock mínimo cero es válido")
    void actualizarStockMinimo_cero_permitido() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(itemInventarioRepository.save(item)).thenReturn(item);

        ItemInventario actualizado = itemInventarioService.actualizarStockMinimo(
                100L,
                0,
                usuario(1L, Rol.ADMIN)
        );

        assertEquals(0, actualizado.getStockMinimo());
        verify(itemInventarioRepository).save(item);
    }

    @Test
    @DisplayName("Configurar stock mínimo null es rechazado")
    void actualizarStockMinimo_null_rechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> itemInventarioService.actualizarStockMinimo(100L, null, usuario(1L, Rol.ADMIN)));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Configurar stock mínimo de item inexistente informa recurso no encontrado")
    void actualizarStockMinimo_itemInexistente_recursoNoEncontrado() {
        when(itemInventarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> itemInventarioService.actualizarStockMinimo(999L, 4, usuario(1L, Rol.ADMIN)));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("COMERCIO no puede modificar stock mínimo de otro comercio")
    void actualizarStockMinimo_comercioAjeno_rechazado() {
        ItemInventario item = item(100L, inventario(10L, comercio(2L)),
                producto(20L, comercio(2L)), deposito(30L, comercio(2L)), 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(comercioService.obtenerPorUsuarioId(1L)).thenReturn(
                ComercioResponseDTO.builder().id(1L).usuarioId(1L).build()
        );

        assertThrows(AccessDeniedException.class,
                () -> itemInventarioService.actualizarStockMinimo(100L, 4, usuario(1L, Rol.COMERCIO)));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("COMERCIO puede modificar stock mínimo de su propio comercio")
    void actualizarStockMinimo_comercioPropio_permitido() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));
        when(comercioService.obtenerPorUsuarioId(1L)).thenReturn(
                ComercioResponseDTO.builder().id(1L).usuarioId(1L).build()
        );
        when(itemInventarioRepository.save(item)).thenReturn(item);

        ItemInventario actualizado = itemInventarioService.actualizarStockMinimo(
                100L,
                4,
                usuario(1L, Rol.COMERCIO)
        );

        assertEquals(4, actualizado.getStockMinimo());
        verify(itemInventarioRepository).save(item);
    }

    @Test
    @DisplayName("DEPOSITO no puede configurar stock mínimo")
    void actualizarStockMinimo_deposito_rechazado() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 5);

        when(itemInventarioRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class,
                () -> itemInventarioService.actualizarStockMinimo(100L, 4, usuario(3L, Rol.DEPOSITO)));

        verify(itemInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("COMERCIO consulta solo alertas de su comercio")
    void obtenerAlertasStock_comercioSoloPropias() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 4);

        when(comercioService.obtenerPorUsuarioId(1L)).thenReturn(
                ComercioResponseDTO.builder().id(1L).usuarioId(1L).build()
        );
        when(itemInventarioRepository.findAlertasStockByComercioId(1L)).thenReturn(List.of(item));

        List<ItemInventario> alertas = itemInventarioService.obtenerAlertasStock(usuario(1L, Rol.COMERCIO));

        assertEquals(1, alertas.size());
        verify(itemInventarioRepository).findAlertasStockByComercioId(1L);
    }

    @Test
    @DisplayName("DEPOSITO consulta solo alertas de su depósito")
    void obtenerAlertasStock_depositoSoloPropias() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 4);

        when(depositoService.obtenerPorUsuarioId(3L)).thenReturn(
                DepositoResponseDTO.builder().id(30L).usuarioId(3L).build()
        );
        when(itemInventarioRepository.findAlertasStockByDepositoId(30L)).thenReturn(List.of(item));

        List<ItemInventario> alertas = itemInventarioService.obtenerAlertasStock(usuario(3L, Rol.DEPOSITO));

        assertEquals(1, alertas.size());
        verify(itemInventarioRepository).findAlertasStockByDepositoId(30L);
    }

    @Test
    @DisplayName("REPARTIDOR no accede a alertas de inventario")
    void obtenerAlertasStock_repartidor_rechazado() {
        assertThrows(AccessDeniedException.class,
                () -> itemInventarioService.obtenerAlertasStock(usuario(4L, Rol.REPARTIDOR)));
    }

    @Test
    @DisplayName("ADMIN consulta alertas globales")
    void obtenerAlertasStock_adminGlobal() {
        ItemInventario item = item(100L, inventario(10L, comercio(1L)),
                producto(20L, comercio(1L)), deposito(30L, comercio(1L)), 4);

        when(itemInventarioRepository.findAlertasStock()).thenReturn(List.of(item));

        List<ItemInventario> alertas = itemInventarioService.obtenerAlertasStock(usuario(1L, Rol.ADMIN));

        assertEquals(1, alertas.size());
        verify(itemInventarioRepository).findAlertasStock();
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
        item.setStockMinimo(5);
        return item;
    }

    private Usuario usuario(Long id, Rol rol) {
        return Usuario.builder()
                .id(id)
                .email("usuario" + id + "@logired.com")
                .password("test")
                .nombre("Usuario")
                .apellido(rol.name())
                .DNI("3000000" + id)
                .telefono("1100000000")
                .rol(rol)
                .build();
    }
}
