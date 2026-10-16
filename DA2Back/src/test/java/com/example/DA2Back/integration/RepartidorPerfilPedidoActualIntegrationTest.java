package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.*;
import com.example.DA2Back.Seguridad.negocio.JwtService;
import com.example.DA2Back.comercio.dato.*;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.pedidos.dato.*;
import com.example.DA2Back.producto.dato.*;
import com.example.DA2Back.repartidor.dato.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:perfilpedidoactual;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RepartidorPerfilPedidoActualIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwt;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private RepartidorRepository repartidores;
    @Autowired private PedidosRepository pedidos;
    @Autowired private ComercioRepository comercios;
    @Autowired private ProductoRepository productos;
    @Autowired private DepositoRepository depositos;
    @Autowired private JdbcTemplate jdbc;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "Origen real")
    void perfilConservaDetallesDuranteCicloYRecargas(String origen) throws Exception {
        Usuario usuario = usuario();
        Repartidor repartidor = perfil(usuario);
        Comercio comercio = comercios.save(Comercio.builder().usuarioId(usuario.getId())
                .nombreComercial("Prueba").razonSocial("Prueba").CUIT(UUID.randomUUID().toString())
                .direccion("Direccion").telefono("123").email(usuario.getEmail()).build());
        Producto producto = productos.save(new Producto(null, "SKU", "Producto real", null,
                "Categoria", EstadoProducto.ACTIVO, comercio));
        Deposito deposito = deposito(comercio, "Deposito del pedido", "Direccion del deposito");
        Pedido pedido = pedido(repartidor, origen);
        pedido.setComercioId(comercio.getId());
        pedido.setDepositoId(deposito.getId());
        pedidos.saveAndFlush(pedido);
        jdbc.update("insert into detalles_pedido (pedido_id, producto_id, cantidad) values (?, ?, ?)",
                pedido.getId(), producto.getId(), 3);
        Long detalleId = jdbc.queryForObject("select id from detalles_pedido where pedido_id = ?", Long.class, pedido.getId());
        String token = "Bearer " + jwt.generateToken(usuario);

        String[] estados = {"ASIGNADO", "RETIRADO", "EN_CAMINO"};
        String[] acciones = {"retirar", "iniciar-viaje", "entregar"};
        for (int i = 0; i < estados.length; i++) {
            // Dos requests independientes: no dependemos de la respuesta del PATCH ni de una sesion JPA anterior.
            for (int recarga = 0; recarga < 2; recarga++) {
                ResultActions respuesta = mvc.perform(get("/repartidor/me").header("Authorization", token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.pedidoActual.id").value(pedido.getId().intValue()))
                        .andExpect(jsonPath("$.pedidoActual.estado").value(estados[i]))
                        .andExpect(jsonPath("$.pedidoActual.depositoId").value(deposito.getId().intValue()))
                        .andExpect(jsonPath("$.pedidoActual.depositoNombre").value("Deposito del pedido"))
                        .andExpect(jsonPath("$.pedidoActual.depositoDireccion").value("Direccion del deposito"))
                        .andExpect(jsonPath("$.pedidoActual.direccionDestino").value("Destino real"))
                        .andExpect(jsonPath("$.pedidoActual.fechaCreacion").value("2026-10-15T10:30:00"))
                        .andExpect(jsonPath("$.pedidoActual.detalles[0].id").value(detalleId.intValue()))
                        .andExpect(jsonPath("$.pedidoActual.detalles[0].productoId").value(producto.getId().intValue()))
                        .andExpect(jsonPath("$.pedidoActual.detalles[0].productoNombre").value("Producto real"))
                        .andExpect(jsonPath("$.pedidoActual.detalles[0].cantidad").value(3));
                if (origen == null) respuesta.andExpect(jsonPath("$.pedidoActual.direccionOrigen").value(nullValue()));
                else respuesta.andExpect(jsonPath("$.pedidoActual.direccionOrigen").value(origen));
            }
            mvc.perform(patch("/repartidor/me/pedidos/{id}/{accion}", pedido.getId(), acciones[i])
                    .header("Authorization", token)).andExpect(status().isOk());
        }
        mvc.perform(get("/repartidor/me").header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$.pedidoActual").value(nullValue()));
        assertEquals(3, jdbc.queryForObject("select count(*) from historial_estado_pedido where pedido_id = ?",
                Integer.class, pedido.getId()));
    }

    @Test
    void perfilLeePedidoConHistorialLegacySinProductos() throws Exception {
        Usuario usuario = usuario();
        Pedido pedido = pedido(perfil(usuario), null);
        jdbc.update("insert into historial_estado_pedido (pedido_id, estado, fecha_hora) values (?, ?, ?)",
                pedido.getId(), "PENDIENTE_COTIZACION", LocalDateTime.now());
        mvc.perform(get("/repartidor/me").header("Authorization", "Bearer " + jwt.generateToken(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoActual.id").value(pedido.getId().intValue()))
                .andExpect(jsonPath("$.pedidoActual.detalles").isEmpty())
                .andExpect(jsonPath("$.pedidoActual.direccionOrigen").value(nullValue()));
        assertEquals("PENDIENTE_COTIZACION", jdbc.queryForObject(
                "select estado from historial_estado_pedido where pedido_id = ?", String.class, pedido.getId()));
    }

    @Test
    void depositoInexistenteNoImpideConsultarElPerfil() throws Exception {
        Usuario usuario = usuario();
        Pedido pedido = pedido(perfil(usuario), "Origen independiente");
        pedido.setDepositoId(Long.MAX_VALUE);
        pedidos.saveAndFlush(pedido);

        mvc.perform(get("/repartidor/me").header("Authorization", "Bearer " + jwt.generateToken(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoActual.id").value(pedido.getId().intValue()))
                .andExpect(jsonPath("$.pedidoActual.depositoId").value(Long.MAX_VALUE))
                .andExpect(jsonPath("$.pedidoActual.depositoNombre").value(nullValue()))
                .andExpect(jsonPath("$.pedidoActual.depositoDireccion").value(nullValue()))
                .andExpect(jsonPath("$.pedidoActual.direccionOrigen").value("Origen independiente"));
    }

    @Test
    void perfilSinPedidoActivoSigueDisponible() throws Exception {
        Usuario usuario = usuario();
        Repartidor repartidor = perfil(usuario);
        repartidor.setEstado(EstadoRepartidor.DISPONIBLE);
        repartidores.saveAndFlush(repartidor);

        mvc.perform(get("/repartidor/me").header("Authorization", "Bearer " + jwt.generateToken(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$.pedidoActual").value(nullValue()));
    }

    @Test
    void perfilSoloExponeDepositoDelPedidoPropioAunqueSeManipulenIds() throws Exception {
        Comercio comercio = comercio(usuario(Rol.COMERCIO));
        Usuario usuarioA = usuario();
        Usuario usuarioB = usuario();
        Deposito depositoA = deposito(comercio, "Deposito A", "Direccion A");
        Deposito depositoB = deposito(comercio, "Deposito B", "Direccion B");
        Pedido pedidoA = pedido(perfil(usuarioA), "Origen A");
        pedidoA.setComercioId(comercio.getId());
        pedidoA.setDepositoId(depositoA.getId());
        pedidos.saveAndFlush(pedidoA);
        Pedido pedidoB = pedido(perfil(usuarioB), "Origen B");
        pedidoB.setComercioId(comercio.getId());
        pedidoB.setDepositoId(depositoB.getId());
        pedidos.saveAndFlush(pedidoB);

        mvc.perform(get("/repartidor/me")
                        .param("repartidorId", pedidoB.getRepartidor().getId().toString())
                        .param("depositoId", depositoB.getId().toString())
                        .header("Authorization", "Bearer " + jwt.generateToken(usuarioA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoActual.id").value(pedidoA.getId().intValue()))
                .andExpect(jsonPath("$.pedidoActual.depositoId").value(depositoA.getId().intValue()))
                .andExpect(jsonPath("$.pedidoActual.depositoNombre").value("Deposito A"))
                .andExpect(jsonPath("$.pedidoActual.depositoDireccion").value("Direccion A"))
                .andExpect(jsonPath("$.pedidoActual.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.pedidoActual.usuarioEmail").doesNotExist());
        mvc.perform(get("/repartidor/me").header("Authorization", "Bearer " + jwt.generateToken(usuarioB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoActual.id").value(pedidoB.getId().intValue()))
                .andExpect(jsonPath("$.pedidoActual.depositoNombre").value("Deposito B"))
                .andExpect(jsonPath("$.pedidoActual.depositoDireccion").value("Direccion B"));
    }

    private Usuario usuario() {
        return usuario(Rol.REPARTIDOR);
    }

    private Usuario usuario(Rol rol) {
        return usuarios.save(Usuario.builder().email(UUID.randomUUID() + "@perfil.test").password("unused")
                .DNI(UUID.randomUUID().toString()).nombre("Test").apellido("Perfil").telefono("123")
                .rol(rol).estado(EstadoUsuario.VALIDADO).build());
    }

    private Comercio comercio(Usuario usuario) {
        return comercios.save(Comercio.builder().usuarioId(usuario.getId())
                .nombreComercial("Comercio de prueba").razonSocial("Prueba").CUIT(UUID.randomUUID().toString())
                .direccion("Direccion").telefono("123").email(usuario.getEmail()).build());
    }

    private Deposito deposito(Comercio comercio, String nombre, String direccion) {
        Deposito deposito = new Deposito();
        deposito.setNombre(nombre);
        deposito.setDireccion(direccion);
        deposito.setComercio(comercio);
        deposito.setUsuarioId(usuario(Rol.DEPOSITO).getId());
        return depositos.saveAndFlush(deposito);
    }

    private Repartidor perfil(Usuario usuario) {
        return repartidores.save(Repartidor.builder().usuarioId(usuario.getId()).patente(UUID.randomUUID().toString())
                .zona("Test").activo(true).estado(EstadoRepartidor.EN_ENTREGA).build());
    }

    private Pedido pedido(Repartidor repartidor, String origen) {
        return pedidos.save(Pedido.builder().comercioId(1L).depositoId(1L).direccionOrigen(origen)
                .direccionDestino("Destino real").fechaCreacion(LocalDateTime.of(2026, 10, 15, 10, 30))
                .estado(EstadoPedido.ASIGNADO).repartidor(repartidor).build());
    }
}
