package com.example.DA2Back.integration;

import com.example.DA2Back.pedidos.dato.EstadoHistoricoPedido;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:historiallegacy;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PedidoHistorialLegacyIntegrationTest {
    @Autowired private PedidosRepository pedidos;
    @Autowired private ServicioDePedidos servicio;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private MockMvc mvc;
    @Autowired private ComercioRepository comercios;
    @Autowired private DepositoRepository depositos;
    private Long comercioId;
    private Long depositoId;
    private final Usuario admin = Usuario.builder().id(1L).rol(Rol.ADMIN).estado(EstadoUsuario.VALIDADO).build();

    @BeforeEach
    void prepararAsociaciones() {
        var comercio = comercios.save(Comercio.builder().usuarioId(100L)
                .nombreComercial("Legacy").razonSocial("Legacy").direccion("Direccion")
                .CUIT("legacy").telefono("123").email("legacy@test.com").build());
        var deposito = new Deposito();
        deposito.setNombre("Legacy");
        deposito.setDireccion("Direccion");
        deposito.setUsuarioId(101L);
        deposito.setComercio(comercio);
        comercioId = comercio.getId();
        depositoId = depositos.save(deposito).getId();
    }

    @Test
    void listadoYDetalleConservanHistorialLegacyYActualEnJson() throws Exception {
        Long antiguo = guardarConHistorial("PENDIENTE_COTIZACION");
        CrearPedidoDTO creacion = new CrearPedidoDTO();
        creacion.setComercioId(comercioId);
        creacion.setDepositoId(depositoId);
        creacion.setDireccionDestino("Destino de pedido nuevo");
        Long nuevo = servicio.crearPedido(creacion, admin).getId();
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/pedidos").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].historial[*].estado", hasItem("PENDIENTE_COTIZACION")))
                .andExpect(jsonPath("$[*].historial[*].estado", hasItem("CREADO")));
        for (Long id : new Long[]{antiguo, nuevo}) {
            mvc.perform(get("/api/pedidos/{id}", id).with(user(admin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.estado").value("CREADO"))
                    .andExpect(jsonPath("$.historial[0].id").isNumber())
                    .andExpect(jsonPath("$.historial[0].fechaHora").isString())
                    .andExpect(jsonPath("$.historial[0].estado").value(
                            id.equals(antiguo) ? "PENDIENTE_COTIZACION" : "CREADO"));
        }
    }

    @Test
    void transicionNuevaNoReescribeHistoriaAnterior() {
        Long id = guardarConHistorial("PENDIENTE_COTIZACION");
        entityManager.clear();
        servicio.marcarListoParaRetirar(id, admin);
        entityManager.flush();
        entityManager.clear();

        var dto = servicio.obtenerPorId(id, admin);
        assertEquals(EstadoPedido.LISTO_PARA_RETIRAR, dto.getEstado());
        assertEquals(2, dto.getHistorial().size());
        assertTrue(dto.getHistorial().stream().anyMatch(h ->
                h.getEstado() == EstadoHistoricoPedido.PENDIENTE_COTIZACION));
        assertTrue(dto.getHistorial().stream().anyMatch(h ->
                h.getEstado() == EstadoHistoricoPedido.LISTO_PARA_RETIRAR));
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from historial_estado_pedido where pedido_id = ? and estado = 'PENDIENTE_COTIZACION'",
                Integer.class, id));
    }

    @Test
    void estadosOperativosSiguenCerradosYTodosTienenRepresentacionHistorica() {
        assertEquals(7, EstadoPedido.values().length);
        assertThrows(IllegalArgumentException.class,
                () -> EstadoPedido.valueOf("PENDIENTE_COTIZACION"));
        for (EstadoPedido estado : EstadoPedido.values()) {
            assertEquals(estado.name(), EstadoHistoricoPedido.desdeOperativo(estado).name());
        }
    }

    private Long guardarConHistorial(String estadoHistorico) {
        Pedido pedido = pedidos.saveAndFlush(Pedido.builder()
                .comercioId(comercioId).depositoId(depositoId)
                .direccionDestino("Destino de prueba")
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.CREADO).build());
        // Insercion SQL para reproducir datos previos, sin pasar por el enum Java.
        jdbc.update("insert into historial_estado_pedido (pedido_id, estado, fecha_hora) values (?, ?, ?)",
                pedido.getId(), estadoHistorico, LocalDateTime.now().minusDays(1));
        return pedido.getId();
    }
}
