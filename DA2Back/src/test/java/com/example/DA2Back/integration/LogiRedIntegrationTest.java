package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de integración end-to-end de LogiRed — adaptada para main.
 *
 * Ejecuta sobre base de datos H2 en memoria (perfil "test") con el contexto
 * Spring completo. Los tests corren en orden y comparten estado de instancia
 * gracias a @TestInstance(PER_CLASS).
 *
 * Nota: usa /auth/** (mapping de main) en lugar de /api/auth/**.
 * No depende de ComercioRepository ya que ese módulo no tiene
 * repositorio en main. El comercioId del pedido es un Long simple.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogiRedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidosRepository pedidosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Estado compartido entre tests ordenados */
    private String tokenJwt;
    private Long pedidoId;
    private final Long COMERCIO_ID = 1L; // ID referencial, no FK real

    // ------------------------------------------------------------------
    // Semilla de datos (una vez antes de todos los tests)
    // ------------------------------------------------------------------

    @BeforeAll
    void sembrarDatos() {
        if (!usuarioRepository.existsByEmail("admin@logired.com")) {
            usuarioRepository.save(Usuario.builder()
                    .username("Administrador LogiRed")
                    .email("admin@logired.com")
                    .password(passwordEncoder.encode("Admin1234!"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build());
        }
    }

    // ==================================================================
    // 1. Acceso sin token debe ser rechazado
    // ==================================================================

    @Test
    @Order(1)
    @DisplayName("GET /api/pedidos sin token debe retornar 401 o 403")
    void accesoSinToken_debeSerRechazado() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/pedidos sin token debe retornar 401 o 403")
    void crearPedidoSinToken_debeSerRechazado() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comercioId\":1,\"direccionDestino\":\"Test\"}"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ==================================================================
    // 2. Login devuelve token JWT valido
    // ==================================================================

    @Test
    @Order(3)
    @DisplayName("POST /auth/login con credenciales correctas devuelve HTTP 200 y token")
    void login_credencialesCorrectas_devuelveToken() throws Exception {
        String body = """
                {
                  "email": "admin@logired.com",
                  "password": "Admin1234!"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenJwt = json.get("token").asText();

        assertNotNull(tokenJwt);
        assertFalse(tokenJwt.isBlank());
    }

    @Test
    @Order(4)
    @DisplayName("POST /auth/login con credenciales incorrectas devuelve error")
    void login_credencialesIncorrectas_devuelveError() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@logired.com\",\"password\":\"mal\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ==================================================================
    // 3. PEDIDOS — CRUD con token JWT
    // ==================================================================

    @Test
    @Order(5)
    @DisplayName("POST /api/pedidos crea un pedido en estado CREADO (HTTP 201)")
    void crearPedido_conToken_devuelve201() throws Exception {
        assertNotNull(tokenJwt, "Requiere token — ejecutar test de login primero");

        String body = String.format("""
                {
                  "comercioId": %d,
                  "direccionDestino": "Av. Rivadavia 5000, CABA"
                }
                """, COMERCIO_ID);

        MvcResult result = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.comercioId", is(COMERCIO_ID.intValue())))
                .andExpect(jsonPath("$.estado", is("CREADO")))
                .andReturn();

        pedidoId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/pedidos lista todos los pedidos")
    void listarPedidos_conToken_devuelve200() throws Exception {
        assertNotNull(tokenJwt);

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/pedidos/{id} retorna el pedido correcto")
    void obtenerPedidoPorId_devuelve200() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        mockMvc.perform(get("/api/pedidos/" + pedidoId)
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$.estado", is("CREADO")));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/pedidos/comercio/{id} filtra por comercioId")
    void listarPorComercio_devuelve200() throws Exception {
        assertNotNull(tokenJwt);

        mockMvc.perform(get("/api/pedidos/comercio/" + COMERCIO_ID)
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comercioId", is(COMERCIO_ID.intValue())));
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/pedidos/estado/CREADO filtra por estado")
    void listarPorEstado_devuelve200() throws Exception {
        assertNotNull(tokenJwt);

        mockMvc.perform(get("/api/pedidos/estado/CREADO")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("CREADO")));
    }

    @Test
    @Order(10)
    @DisplayName("PATCH /api/pedidos/{id}/estado actualiza a EN_CAMINO y persiste en BD")
    void actualizarEstado_persiste() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content("{\"estado\": \"EN_CAMINO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_CAMINO")));

        Pedido bd = pedidosRepository.findById(pedidoId).orElseThrow();
        assertEquals(EstadoPedido.EN_CAMINO, bd.getEstado());
    }

    @Test
    @Order(11)
    @DisplayName("DELETE /api/pedidos/{id}/cancelar cancela el pedido y persiste en BD")
    void cancelarPedido_persiste() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        mockMvc.perform(delete("/api/pedidos/" + pedidoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));

        Pedido bd = pedidosRepository.findById(pedidoId).orElseThrow();
        assertEquals(EstadoPedido.CANCELADO, bd.getEstado());
    }

    @Test
    @Order(12)
    @DisplayName("DELETE /api/pedidos/{id}/cancelar sobre ENTREGADO devuelve 409 Conflict")
    void cancelarEntregado_devuelve409() throws Exception {
        assertNotNull(tokenJwt);

        // Crear nuevo pedido y llevarlo a ENTREGADO
        MvcResult crear = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content("{\"comercioId\":1,\"direccionDestino\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Long idEntregado = objectMapper
                .readTree(crear.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(patch("/api/pedidos/" + idEntregado + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenJwt)
                .content("{\"estado\": \"ENTREGADO\"}"))
                .andExpect(status().isOk());

        // Intentar cancelar — debe retornar 409
        mockMvc.perform(delete("/api/pedidos/" + idEntregado + "/cancelar")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("entregado")));
    }
}
