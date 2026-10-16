package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.negocio.JwtService;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.InventarioRepository;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sin transaccion externa de test: cada request debe confirmar o revertir su propia transaccion.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pedidosownership;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoOwnershipIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ComercioRepository comercios;
    @Autowired private DepositoRepository depositos;
    @Autowired private ProductoRepository productos;
    @Autowired private InventarioRepository inventarios;
    @Autowired private ItemInventarioRepository items;
    @Autowired private PedidosRepository pedidos;
    @Autowired private JwtService jwt;
    @Autowired private RepartidorRepository repartidores;
    private final ObjectMapper json = new ObjectMapper();
    private Usuario admin, usuarioA, usuarioB, encargadoA, encargadoA2, encargadoB, repartidor;
    private Comercio comercioA, comercioB;
    private Deposito depositoA, depositoA2, depositoB;
    private Producto productoA, productoB;
    private ItemInventario stockA, stockA2, stockB;
    private Pedido creadoA, creadoA2, creadoB, listoA, listoA2, listoB;

    @BeforeEach
    void prepararDatosAislados() {
        // Esta clase usa exclusivamente su propia H2 en memoria.
        for (String tabla : List.of("historial_estado_pedido", "detalles_pedido", "pedidos",
                "items_inventario", "inventarios", "productos", "depositos", "comercios", "repartidores", "usuarios")) {
            jdbc.update("delete from " + tabla);
        }
        admin = usuario("admin", Rol.ADMIN);
        usuarioA = usuario("comercio-a", Rol.COMERCIO);
        usuarioB = usuario("comercio-b", Rol.COMERCIO);
        encargadoA = usuario("deposito-a", Rol.DEPOSITO);
        encargadoA2 = usuario("deposito-a2", Rol.DEPOSITO);
        encargadoB = usuario("deposito-b", Rol.DEPOSITO);
        repartidor = usuario("repartidor", Rol.REPARTIDOR);
        comercioA = comercio(usuarioA);
        comercioB = comercio(usuarioB);
        depositoA = deposito(comercioA, encargadoA);
        depositoA2 = deposito(comercioA, encargadoA2);
        depositoB = deposito(comercioB, encargadoB);
        productoA = producto(comercioA);
        productoB = producto(comercioB);
        Inventario invA = inventario(comercioA);
        Inventario invB = inventario(comercioB);
        stockA = stock(invA, productoA, depositoA);
        stockA2 = stock(invA, productoA, depositoA2);
        stockB = stock(invB, productoB, depositoB);
        creadoA = pedido(comercioA, depositoA, productoA, EstadoPedido.CREADO, "PENDIENTE_COTIZACION");
        creadoA2 = pedido(comercioA, depositoA2, productoA, EstadoPedido.CREADO, "CREADO");
        creadoB = pedido(comercioB, depositoB, productoB, EstadoPedido.CREADO, "CREADO");
        listoA = pedido(comercioA, depositoA, productoA, EstadoPedido.LISTO_PARA_RETIRAR, "LISTO_PARA_RETIRAR");
        listoA2 = pedido(comercioA, depositoA2, productoA, EstadoPedido.LISTO_PARA_RETIRAR, "LISTO_PARA_RETIRAR");
        listoB = pedido(comercioB, depositoB, productoB, EstadoPedido.LISTO_PARA_RETIRAR, "LISTO_PARA_RETIRAR");
    }

    @Test
    void adminConsultaTodosLosAmbitos() throws Exception {
        verificarLista(admin, "/api/pedidos", creadoA, creadoA2, creadoB, listoA, listoA2, listoB);
        verificarLista(admin, "/api/pedidos/comercio/" + comercioB.getId(), creadoB, listoB);
        verificarLista(admin, "/api/pedidos/estado/CREADO", creadoA, creadoA2, creadoB);
        verificarLista(admin, "/api/pedidos/pendientes", listoA, listoA2, listoB);
        mvc.perform(autenticado(get("/api/pedidos/{id}", creadoB.getId()), admin)).andExpect(status().isOk());
    }

    @Test
    void comercioSoloLeeSusPedidosEnTodosLosListados() throws Exception {
        verificarLista(usuarioA, "/api/pedidos", creadoA, creadoA2, listoA, listoA2);
        verificarLista(usuarioB, "/api/pedidos", creadoB, listoB);
        verificarLista(usuarioA, "/api/pedidos/comercio/" + comercioA.getId(), creadoA, creadoA2, listoA, listoA2);
        verificarLista(usuarioA, "/api/pedidos/estado/CREADO", creadoA, creadoA2);
        verificarLista(usuarioA, "/api/pedidos/pendientes", listoA, listoA2);
        mvc.perform(autenticado(get("/api/pedidos/{id}", creadoA.getId()), usuarioA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historial[0].estado").value("PENDIENTE_COTIZACION"));
    }

    @Test
    void comercioNoPuedeManipularIdsParaLeerAjeno() throws Exception {
        mvc.perform(autenticado(get("/api/pedidos/{id}", creadoB.getId()), usuarioA)).andExpect(status().isForbidden());
        mvc.perform(autenticado(get("/api/pedidos/comercio/{id}", comercioB.getId()), usuarioA)).andExpect(status().isForbidden());
        verificarLista(usuarioA, "/api/pedidos?comercioId=" + comercioB.getId(), creadoA, creadoA2, listoA, listoA2);
    }

    @Test
    void depositoSoloLeeSuDepositoInclusoDentroDelMismoComercio() throws Exception {
        verificarLista(encargadoA, "/api/pedidos", creadoA, listoA);
        verificarLista(encargadoA2, "/api/pedidos", creadoA2, listoA2);
        verificarLista(encargadoA, "/api/pedidos/comercio/" + comercioA.getId(), creadoA, listoA);
        verificarLista(encargadoA, "/api/pedidos/estado/CREADO", creadoA);
        verificarLista(encargadoA, "/api/pedidos/pendientes", listoA);
        mvc.perform(autenticado(get("/api/pedidos/{id}", creadoA.getId()), encargadoA)).andExpect(status().isOk());
        for (Pedido ajeno : List.of(creadoA2, creadoB)) {
            mvc.perform(autenticado(get("/api/pedidos/{id}", ajeno.getId()), encargadoA)).andExpect(status().isForbidden());
        }
        mvc.perform(autenticado(get("/api/pedidos/comercio/{id}", comercioB.getId()), encargadoA)).andExpect(status().isForbidden());
    }

    @Test
    void repartidorNoPuedeUsarNingunaConsultaGeneral() throws Exception {
        for (String ruta : List.of("/api/pedidos", "/api/pedidos/" + creadoA.getId(),
                "/api/pedidos/comercio/" + comercioA.getId(), "/api/pedidos/estado/CREADO", "/api/pedidos/pendientes")) {
            mvc.perform(autenticado(get(ruta), repartidor)).andExpect(status().isForbidden());
        }
    }

    @Test
    void sinAsociacionNoHayFallbackGlobalNiEscrituras() throws Exception {
        var antes = snapshot();
        for (Rol rol : List.of(Rol.COMERCIO, Rol.DEPOSITO)) {
            Usuario sinPerfil = usuario("sin-" + rol, rol);
            for (String ruta : List.of("/api/pedidos", "/api/pedidos/" + creadoA.getId(),
                    "/api/pedidos/comercio/" + comercioA.getId(), "/api/pedidos/estado/CREADO", "/api/pedidos/pendientes")) {
                mvc.perform(autenticado(get(ruta), sinPerfil)).andExpect(status().isForbidden());
            }
            mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoA.getId()), sinPerfil))
                    .andExpect(status().isForbidden());
            mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoA.getId()), sinPerfil))
                    .andExpect(status().isForbidden());
            mvc.perform(autenticado(postPedido(body(comercioA, depositoA, productoA)), sinPerfil))
                    .andExpect(status().isForbidden());
        }
        assertEquals(antes, snapshot());
    }

    @Test
    void noAutenticadoYRecursoInexistente() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().is4xxClientError());
        mvc.perform(autenticado(get("/api/pedidos/999999"), usuarioA)).andExpect(status().isNotFound());
    }

    @Test
    void crearDerivaComercioDelJwtYDescuentaSoloStockPropio() throws Exception {
        String body = "{\"depositoId\":" + depositoA.getId() + ",\"direccionDestino\":\"Destino\","
                + "\"detalles\":[{\"productoId\":" + productoA.getId() + ",\"cantidad\":2}]}";
        var resultado = mvc.perform(autenticado(postPedido(body), usuarioA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comercioId").value(comercioA.getId()))
                .andExpect(jsonPath("$.estado").value("CREADO")).andReturn();
        Long id = json.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(autenticado(get("/api/pedidos/{id}", id), usuarioA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.historial[0].estado").value("CREADO"));
        assertEquals(8, items.findById(stockA.getId()).orElseThrow().getCantidad());
        assertEquals(10, items.findById(stockA2.getId()).orElseThrow().getCantidad());
        assertEquals(10, items.findById(stockB.getId()).orElseThrow().getCantidad());
    }

    @Test
    void crearConComercioDepositoOProductoAjenoNoModificaNada() throws Exception {
        var antes = snapshot();
        for (String body : List.of(body(comercioB, depositoB, productoB),
                body(comercioA, depositoB, productoA), body(comercioA, depositoA, productoB))) {
            mvc.perform(autenticado(postPedido(body), usuarioA)).andExpect(status().isForbidden());
            assertEquals(antes, snapshot());
        }
    }

    @Test
    void detalleAjenoDespuesDeDetalleValidoNoDescuentaStock() throws Exception {
        var antes = snapshot();
        String body = "{\"comercioId\":" + comercioA.getId() + ",\"depositoId\":" + depositoA.getId()
                + ",\"direccionDestino\":\"Destino\",\"detalles\":[{\"productoId\":" + productoA.getId()
                + ",\"cantidad\":2},{\"productoId\":" + productoB.getId() + ",\"cantidad\":2}]}";
        mvc.perform(autenticado(postPedido(body), usuarioA)).andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
    }

    @Test
    void itemHistoricoIncoherenteNoPermiteDescontarStockAjeno() throws Exception {
        // Simula una fila legacy que vincula inventario A con producto B.
        stock(stockA.getInventario(), productoB, depositoA);
        var antes = snapshot();
        mvc.perform(autenticado(postPedido(body(comercioA, depositoA, productoB)), usuarioA))
                .andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
    }

    @Test
    void cantidadesInvalidasNoAlteranStock() throws Exception {
        var antes = snapshot();
        for (String cantidad : List.of("-1", "0", "null")) {
            String body = body(comercioA, depositoA, productoA).replace("\"cantidad\":2", "\"cantidad\":" + cantidad);
            mvc.perform(autenticado(postPedido(body), usuarioA)).andExpect(status().isBadRequest());
        }
        assertEquals(antes, snapshot());
    }

    @Test
    void comercioNoPuedeCancelarNiPrepararPedidoAjeno() throws Exception {
        var antes = snapshot();
        mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoB.getId()), usuarioA))
                .andExpect(status().isForbidden());
        mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoB.getId()), usuarioA))
                .andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
    }

    @Test
    void cancelarAjenoNoLiberaAlRepartidorAsignado() throws Exception {
        var perfil = repartidores.save(Repartidor.builder().usuarioId(repartidor.getId())
                .patente("TEST123").zona("Test").activo(true).estado(EstadoRepartidor.EN_ENTREGA).build());
        jdbc.update("update pedidos set estado = 'ASIGNADO', repartidor_id = ? where id = ?", perfil.getId(), creadoB.getId());
        var antes = snapshot();
        mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoB.getId()), usuarioA))
                .andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
        assertEquals(EstadoRepartidor.EN_ENTREGA, repartidores.findById(perfil.getId()).orElseThrow().getEstado());
    }

    @Test
    void pendientesExcluyeAsignadosYListaVaciaEsValida() throws Exception {
        var perfil = repartidores.save(Repartidor.builder().usuarioId(repartidor.getId())
                .patente("TEST123").zona("Test").activo(true).estado(EstadoRepartidor.EN_ENTREGA).build());
        jdbc.update("update pedidos set repartidor_id = ? where id = ?", perfil.getId(), listoA.getId());
        verificarLista(usuarioA, "/api/pedidos/pendientes", listoA2);
        verificarLista(encargadoA, "/api/pedidos/pendientes");
        verificarLista(usuarioB, "/api/pedidos/estado/ENTREGADO");
    }

    @Test
    void depositoSinComercioSeRechazaSinAccesoGlobal() throws Exception {
        jdbc.update("update depositos set comercio_id = null where id = ?", depositoA.getId());
        var antes = snapshot();
        mvc.perform(autenticado(get("/api/pedidos"), encargadoA)).andExpect(status().isForbidden());
        mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoA.getId()), encargadoA))
                .andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
    }

    @Test
    void depositoSoloPreparaPedidosPropios() throws Exception {
        var antes = snapshot();
        for (Pedido ajeno : List.of(creadoA2, creadoB)) {
            mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", ajeno.getId()), encargadoA))
                    .andExpect(status().isForbidden());
        }
        assertEquals(antes, snapshot());
        mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoA.getId()), encargadoA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("LISTO_PARA_RETIRAR"));
        assertEquals(10, items.findById(stockA.getId()).orElseThrow().getCantidad());
    }

    @Test
    void depositoYRepartidorNoCreanNiCancelan() throws Exception {
        var antes = snapshot();
        for (Usuario actor : List.of(encargadoA, repartidor)) {
            mvc.perform(autenticado(postPedido(body(comercioA, depositoA, productoA)), actor)).andExpect(status().isForbidden());
            mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoA.getId()), actor)).andExpect(status().isForbidden());
        }
        mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoA.getId()), repartidor))
                .andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
    }

    @Test
    void propietarioPuedePrepararYCancelarConHistorialLegacy() throws Exception {
        mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoA.getId()), usuarioA)).andExpect(status().isOk());
        mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoA.getId()), usuarioA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO"));
        mvc.perform(autenticado(get("/api/pedidos/{id}", creadoA.getId()), usuarioA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.historial[*].estado",
                        containsInAnyOrder("PENDIENTE_COTIZACION", "LISTO_PARA_RETIRAR", "CANCELADO")));
        assertEquals(12, items.findById(stockA.getId()).orElseThrow().getCantidad());
        assertEquals(10, items.findById(stockB.getId()).orElseThrow().getCantidad());
    }

    @Test
    void adminPuedeCrearPrepararYCancelarEnOtroComercio() throws Exception {
        mvc.perform(autenticado(postPedido(body(comercioB, depositoB, productoB)), admin)).andExpect(status().isCreated());
        mvc.perform(autenticado(patch("/api/pedidos/{id}/listo-para-retirar", creadoB.getId()), admin)).andExpect(status().isOk());
        mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoB.getId()), admin)).andExpect(status().isOk());
        assertEquals(10, items.findById(stockB.getId()).orElseThrow().getCantidad());
    }

    @Test
    void pedidoIncoherenteNoPuedeRestaurarStockDeOtroComercio() throws Exception {
        jdbc.update("update detalles_pedido set producto_id = ? where pedido_id = ?", productoB.getId(), creadoA.getId());
        var antes = snapshot();
        mvc.perform(autenticado(delete("/api/pedidos/{id}/cancelar", creadoA.getId()), usuarioA)).andExpect(status().isForbidden());
        assertEquals(antes, snapshot());
    }

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder request, Usuario actor) {
        return request.header("Authorization", "Bearer " + jwt.generateToken(actor));
    }

    private MockHttpServletRequestBuilder postPedido(String body) {
        return post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private String body(Comercio comercio, Deposito deposito, Producto producto) {
        return "{\"comercioId\":" + comercio.getId() + ",\"depositoId\":" + deposito.getId()
                + ",\"direccionDestino\":\"Destino\",\"detalles\":[{\"productoId\":" + producto.getId() + ",\"cantidad\":2}]}";
    }

    private void verificarLista(Usuario actor, String ruta, Pedido... esperados) throws Exception {
        Integer[] ids = java.util.Arrays.stream(esperados).map(p -> p.getId().intValue()).toArray(Integer[]::new);
        mvc.perform(autenticado(get(ruta), actor)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(ids)));
    }

    private List<List<Map<String, Object>>> snapshot() {
        return List.of(jdbc.queryForList("select * from pedidos order by id"),
                jdbc.queryForList("select * from historial_estado_pedido order by id"),
                jdbc.queryForList("select * from detalles_pedido order by id"),
                jdbc.queryForList("select * from items_inventario order by id"),
                jdbc.queryForList("select * from repartidores order by id"));
    }

    private Usuario usuario(String nombre, Rol rol) {
        return usuarios.save(Usuario.builder().email(nombre + "@ownership.test").password("unused")
                .nombre(nombre).apellido("Test").DNI(nombre).telefono("123")
                .rol(rol).estado(EstadoUsuario.VALIDADO).build());
    }

    private Comercio comercio(Usuario usuario) {
        return comercios.save(Comercio.builder().usuarioId(usuario.getId()).nombreComercial(usuario.getNombre())
                .razonSocial("Prueba").direccion("Direccion").CUIT(usuario.getNombre())
                .telefono("123").email(usuario.getEmail()).build());
    }

    private Deposito deposito(Comercio comercio, Usuario encargado) {
        Deposito deposito = new Deposito();
        deposito.setNombre(encargado.getNombre());
        deposito.setDireccion("Direccion");
        deposito.setComercio(comercio);
        deposito.setUsuarioId(encargado.getId());
        return depositos.save(deposito);
    }

    private Producto producto(Comercio comercio) {
        return productos.save(new Producto(null, "SKU", "Producto", "Descripcion", "Categoria", EstadoProducto.ACTIVO, comercio));
    }

    private Inventario inventario(Comercio comercio) {
        Inventario inventario = new Inventario();
        inventario.setComercio(comercio);
        return inventarios.save(inventario);
    }

    private ItemInventario stock(Inventario inventario, Producto producto, Deposito deposito) {
        ItemInventario item = new ItemInventario();
        item.setInventario(inventario);
        item.setProducto(producto);
        item.setDeposito(deposito);
        item.setCantidad(10);
        return items.save(item);
    }

    private Pedido pedido(Comercio comercio, Deposito deposito, Producto producto, EstadoPedido estado, String historial) {
        Pedido pedido = pedidos.saveAndFlush(Pedido.builder().comercioId(comercio.getId()).depositoId(deposito.getId())
                .direccionDestino("Destino").fechaCreacion(LocalDateTime.now()).estado(estado).build());
        jdbc.update("insert into historial_estado_pedido (pedido_id, estado, fecha_hora) values (?, ?, ?)",
                pedido.getId(), historial, LocalDateTime.now());
        jdbc.update("insert into detalles_pedido (pedido_id, producto_id, cantidad) values (?, ?, 2)", pedido.getId(), producto.getId());
        return pedido;
    }
}
