package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.*;
import com.example.DA2Back.Seguridad.negocio.JwtService;
import com.example.DA2Back.comercio.dato.*;
import com.example.DA2Back.cotizacion.dto.*;
import com.example.DA2Back.cotizacion.negocio.ServicioDeCotizacion;
import com.example.DA2Back.deposito.dato.*;
import com.example.DA2Back.inventario.dato.*;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import com.example.DA2Back.pedidos.dato.*;
import com.example.DA2Back.pedidos.dto.*;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.producto.dato.*;
import com.example.DA2Back.repartidor.dato.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sin transaccion externa: se comprueba el commit/rollback real de cada operacion.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stockfinal;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.mail.host=false",
        "app.mensajeria.habilitada=false",
        "app.mapas.proveedor=local"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
class PedidoStockIntegrationTest {
    @Autowired ServicioDePedidos servicio;
    @Autowired UsuarioRepository usuarios;
    @Autowired ComercioRepository comercios;
    @Autowired DepositoRepository depositos;
    @Autowired ProductoRepository productos;
    @Autowired InventarioRepository inventarios;
    @Autowired ItemInventarioRepository items;
    @Autowired PedidosRepository pedidos;
    @Autowired RepartidorRepository repartidores;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}") String secretoDePruebas;
    @MockitoBean ServicioDeCotizacion cotizacion;
    @MockitoBean NotificacionService notificaciones;
    Usuario usuario;
    Comercio comercio;
    Deposito deposito;
    Producto producto;
    ItemInventario item;

    @BeforeEach
    void preparar() {
        String sufijo = UUID.randomUUID().toString();
        usuario = usuarios.save(Usuario.builder().email(sufijo + "@stock.invalid").password("no-usada")
                .nombre("Prueba").apellido("Stock").DNI(sufijo).telefono("000")
                .rol(Rol.COMERCIO).estado(EstadoUsuario.VALIDADO).build());
        comercio = comercios.save(Comercio.builder().usuarioId(usuario.getId()).nombreComercial("Prueba")
                .razonSocial("Prueba").direccion("Destino ficticio").CUIT(sufijo).telefono("000")
                .email(sufijo + "@comercio.invalid").build());
        deposito = new Deposito();
        deposito.setNombre("Deposito ficticio");
        deposito.setDireccion("Origen ficticio");
        deposito.setComercio(comercio);
        Usuario encargado = usuarios.save(Usuario.builder().email(sufijo + "@deposito.invalid").password("no-usada")
                .nombre("Prueba").apellido("Deposito").DNI("deposito-" + sufijo).telefono("000")
                .rol(Rol.DEPOSITO).estado(EstadoUsuario.VALIDADO).build());
        deposito.setUsuarioId(encargado.getId());
        deposito = depositos.save(deposito);
        producto = productos.save(new Producto(null, "SKU", "Producto ficticio", "Prueba", "Prueba",
                EstadoProducto.ACTIVO, comercio));
        Inventario inventario = new Inventario();
        inventario.setComercio(comercio);
        inventario = inventarios.save(inventario);
        item = new ItemInventario();
        item.setInventario(inventario);
        item.setProducto(producto);
        item.setDeposito(deposito);
        item.setCantidad(3);
        item = items.saveAndFlush(item);
        when(cotizacion.cotizar(anyLong(), any(), any(), anyInt())).thenAnswer(invocacion -> {
            // Simula latencia externa sin depender de red; aumenta la superposicion de transacciones.
            Thread.sleep(100);
            return new CotizacionDTO(1, 5, invocacion.getArgument(3), BigDecimal.ONE, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, "ARS", false, true, "LOCAL",
                    new PuntoDTO("Origen ficticio", null, null), new PuntoDTO("Destino ficticio", null, null));
        });
    }

    @Test
    void dobleCancelacionAutenticadaNoDevuelveStockDosVeces() throws Exception {
        Long id = crear(1).getId();
        assertEquals(2, stock());
        String bearer = "Bearer " + jwt.generateToken(usuario);
        mvc.perform(delete("/api/pedidos/{id}/cancelar", id).header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO"));
        mvc.perform(delete("/api/pedidos/{id}/cancelar", id).header("Authorization", bearer))
                .andExpect(status().isConflict());
        assertEquals(3, stock());
        assertEquals(1, historial(id, "CANCELADO"));
    }

    @Test
    void cancelacionSinAutenticacionConJwtInvalidoOExpiradoNoModificaNada() throws Exception {
        Long id = crear(1).getId();
        mvc.perform(delete("/api/pedidos/{id}/cancelar", id)).andExpect(status().isUnauthorized());
        String expirado = io.jsonwebtoken.Jwts.builder().subject(usuario.getEmail())
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 120000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretoDePruebas.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        for (String token : List.of("invalido", "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0.", expirado)) {
            mvc.perform(delete("/api/pedidos/{id}/cancelar", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        }
        assertEquals(2, stock());
        assertEquals(EstadoPedido.CREADO, pedidos.findById(id).orElseThrow().getEstado());
        assertEquals(0, historial(id, "CANCELADO"));
    }

    @Test
    void jwtEmitidoAntesDelBloqueoNoPermiteConsultarProductos() throws Exception {
        String bearer = "Bearer " + jwt.generateToken(usuario);
        usuario.setEstado(EstadoUsuario.BLOQUEADO);
        usuarios.saveAndFlush(usuario);
        mvc.perform(get("/productos").header("Authorization", bearer)).andExpect(status().isUnauthorized());
    }

    @Test
    void ultimaUnidadSoloPuedeVenderseUnaVez() throws Exception {
        item.setCantidad(1);
        items.saveAndFlush(item);
        List<Boolean> resultado = enParalelo(() -> intentarCrear(), () -> intentarCrear());
        assertEquals(1, resultado.stream().filter(Boolean::booleanValue).count());
        assertEquals(0, stock());
        assertEquals(1, pedidos.findByComercioId(comercio.getId()).size());
    }

    @Test
    void cancelacionesConcurrentesDelMismoPedidoRestituyenUnaVez() throws Exception {
        Long id = crear(1).getId();
        var resultado = enParalelo(() -> intentarCancelar(id), () -> intentarCancelar(id));
        assertEquals(1, resultado.stream().filter(Boolean::booleanValue).count());
        assertEquals(3, stock());
        assertEquals(1, historial(id, "CANCELADO"));
    }

    @Test
    void stockInsuficienteNoCreaPedidoNiHistorial() {
        assertThrows(IllegalStateException.class, () -> crear(4));
        assertEquals(3, stock());
        assertTrue(pedidos.findByComercioId(comercio.getId()).isEmpty());
    }

    @Test
    void cancelarSinItemRestaurableRechazaYConservaPedido() {
        Long id = crear(1).getId();
        items.deleteById(item.getId());
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(id, usuario));
        assertEquals(EstadoPedido.CREADO, pedidos.findById(id).orElseThrow().getEstado());
        assertEquals(0, historial(id, "CANCELADO"));
    }

    @Test
    void errorPosteriorALaRestitucionRevierteStockEstadoEHistorial() {
        Long id = crear(1).getId();
        doThrow(new IllegalStateException("Fallo simulado despues del stock"))
                .when(notificaciones).notificarActualizacionPedido(any(Pedido.class), eq(EstadoPedido.CANCELADO));
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(id, usuario));
        assertEquals(2, stock());
        assertEquals(EstadoPedido.CREADO, pedidos.findById(id).orElseThrow().getEstado());
        assertEquals(0, historial(id, "CANCELADO"));
    }

    @Test
    void errorDuranteCreacionRevierteDescuento() {
        doThrow(new IllegalStateException("Fallo simulado durante creacion"))
                .when(notificaciones).notificarSiCruzoStockMinimo(any(), anyInt(), anyInt());
        assertThrows(IllegalStateException.class, () -> crear(1));
        assertEquals(3, stock());
        assertTrue(pedidos.findByComercioId(comercio.getId()).isEmpty());
    }

    @Test
    void cancelarEnTodosLosEstadosPermitidosLiberaRepartidorCuandoCorresponde() {
        for (EstadoPedido estado : List.of(EstadoPedido.CREADO, EstadoPedido.LISTO_PARA_RETIRAR,
                EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO)) {
            Long id = crear(1).getId();
            Repartidor repartidor = null;
            if (estado != EstadoPedido.CREADO) servicio.marcarListoParaRetirar(id, usuario);
            if (estado == EstadoPedido.ASIGNADO || estado == EstadoPedido.RETIRADO) {
                repartidor = nuevoRepartidor();
                servicio.asignarRepartidor(id, repartidor.getId());
            }
            if (estado == EstadoPedido.RETIRADO) servicio.marcarRetirado(id);
            servicio.cancelar(id, usuario);
            assertEquals(3, stock());
            assertEquals(EstadoPedido.CANCELADO, pedidos.findById(id).orElseThrow().getEstado());
            if (repartidor != null) {
                assertEquals(EstadoRepartidor.DISPONIBLE,
                        repartidores.findById(repartidor.getId()).orElseThrow().getEstado());
                assertEquals(repartidor.getId(), pedidos.findById(id).orElseThrow().getRepartidor().getId());
            }
        }
    }

    @Test
    void entregadoYCanceladoRechazanTodasLasTransicionesSinCambios() {
        Repartidor repartidor = nuevoRepartidor();
        Long entregado = crear(1).getId();
        servicio.marcarListoParaRetirar(entregado, usuario);
        servicio.asignarRepartidor(entregado, repartidor.getId());
        servicio.marcarRetirado(entregado);
        servicio.iniciarViaje(entregado);
        servicio.entregar(entregado);
        Long cancelado = crear(1).getId();
        servicio.cancelar(cancelado, usuario);
        for (Long id : List.of(entregado, cancelado)) {
            var estado = pedidos.findById(id).orElseThrow().getEstado();
            int antes = stock();
            int registros = jdbc.queryForObject("select count(*) from historial_estado_pedido where pedido_id=?", Integer.class, id);
            for (Runnable accion : List.<Runnable>of(() -> servicio.cancelar(id, usuario),
                    () -> servicio.marcarListoParaRetirar(id, usuario),
                    () -> servicio.asignarRepartidor(id, repartidor.getId()),
                    () -> servicio.marcarRetirado(id), () -> servicio.iniciarViaje(id), () -> servicio.entregar(id))) {
                assertThrows(RuntimeException.class, accion::run);
            }
            assertEquals(estado, pedidos.findById(id).orElseThrow().getEstado());
            assertEquals(antes, stock());
            assertEquals(registros, jdbc.queryForObject("select count(*) from historial_estado_pedido where pedido_id=?", Integer.class, id));
        }
    }

    private PedidoResponseDTO crear(int cantidad) {
        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(comercio.getId());
        dto.setDepositoId(deposito.getId());
        DetallePedidoDTO detalle = new DetallePedidoDTO();
        detalle.setProductoId(producto.getId());
        detalle.setCantidad(cantidad);
        dto.setDetalles(List.of(detalle));
        return servicio.crearPedido(dto, usuario);
    }

    private int stock() { return items.findById(item.getId()).orElseThrow().getCantidad(); }
    private int historial(Long id, String estado) {
        return jdbc.queryForObject("select count(*) from historial_estado_pedido where pedido_id=? and estado=?", Integer.class, id, estado);
    }
    private boolean intentarCrear() { try { crear(1); return true; } catch (IllegalStateException e) { return false; } }
    private boolean intentarCancelar(Long id) {
        try { servicio.cancelar(id, usuario); return true; }
        catch (com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException e) { return false; }
    }
    private Repartidor nuevoRepartidor() {
        String id = UUID.randomUUID().toString();
        Usuario u = usuarios.save(Usuario.builder().email(id + "@repartidor.invalid").password("no-usada")
                .nombre("Prueba").apellido("Entrega").DNI(id).telefono("000")
                .rol(Rol.REPARTIDOR).estado(EstadoUsuario.VALIDADO).build());
        return repartidores.save(Repartidor.builder().usuarioId(u.getId()).patente(id).zona("Prueba")
                .activo(true).estado(EstadoRepartidor.DISPONIBLE).build());
    }
    private List<Boolean> enParalelo(Callable<Boolean> a, Callable<Boolean> b) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Future<Boolean> x = pool.submit(() -> { inicio.await(5, TimeUnit.SECONDS); return a.call(); });
            Future<Boolean> y = pool.submit(() -> { inicio.await(5, TimeUnit.SECONDS); return b.call(); });
            inicio.countDown();
            return List.of(x.get(20, TimeUnit.SECONDS), y.get(20, TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS)); }
    }
}
