package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
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
 * Suite de integración end-to-end de LogiRed.
 *
 * Ejecuta sobre base de datos H2 en memoria (perfil "test") con el contexto
 * Spring completo. Los tests corren en orden y comparten estado de instancia
 * (token JWT, IDs) gracias a @TestInstance(PER_CLASS).
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
    private ComercioRepository comercioRepository;

    @Autowired
    private PedidosRepository pedidosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Shared state between ordered tests */
    private String tokenJwt;
    private Long comercioId;
    private Long pedidoId;

    // ------------------------------------------------------------------
    // Semilla de datos (se ejecuta UNA sola vez antes de todos los tests)
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

        if (!comercioRepository.existsByEmail("comercio@logired.com")) {
            comercioRepository.save(Comercio.builder()
                    .nombre("Pizzeria Central LogiRed")
                    .direccion("Av. Corrientes 1234, CABA")
                    .telefono("1122334455")
                    .email("comercio@logired.com")
                    .build());
        }

        if (!usuarioRepository.existsByEmail("comercio@logired.com")) {
            usuarioRepository.save(Usuario.builder()
                    .username("Comercio Central")
                    .email("comercio@logired.com")
                    .password(passwordEncoder.encode("Admin1234!"))
                    .rol(Rol.COMERCIO)
                    .activo(true)
                    .build());
        }
    }

    // ==================================================================
    // 1. SEGURIDAD — Acceso sin token debe ser rechazado (401/403)
    // ==================================================================

    @Test
    @Order(1)
    @DisplayName("GET /api/pedidos sin token debe retornar 401 o 403")
    void accesoSinToken_pedidos_debeSerRechazado() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/comercios sin token debe retornar 401 o 403")
    void accesoSinToken_comercios_debeSerRechazado() throws Exception {
        mockMvc.perform(get("/api/comercios"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/pedidos sin token debe retornar 401 o 403")
    void crearPedidoSinToken_debeSerRechazado() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comercioId\":1,\"direccionDestino\":\"Test\"}"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // ==================================================================
    // 2. AUTENTICACION — Login devuelve token JWT valido
    // ==================================================================

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login con credenciales correctas devuelve HTTP 200 y token JWT")
    void login_credencialesCorrectas_devuelveToken() throws Exception {
        String body = """
                {
                  "email": "admin@logired.com",
                  "password": "Admin1234!"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenJwt = json.get("token").asText();

        assertNotNull(tokenJwt, "El token JWT no debe ser nulo");
        assertFalse(tokenJwt.isBlank(), "El token JWT no debe estar vacio");
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login con credenciales incorrectas devuelve error (no 200)")
    void login_credencialesIncorrectas_devuelveError() throws Exception {
        String body = """
                {
                  "email": "admin@logired.com",
                  "password": "contrasenaMal"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    // ==================================================================
    // 3. COMERCIOS — CRUD con token JWT
    // ==================================================================

    @Test
    @Order(6)
    @DisplayName("POST /api/comercios con token valido registra un comercio (HTTP 201)")
    void registrarComercio_conToken_devuelve201() throws Exception {
        assertNotNull(tokenJwt, "tokenJwt es null — el test de login debe ejecutarse primero (Order 4)");

        String body = """
                {
                  "nombre": "Sushi Express Test",
                  "direccion": "Av. Santa Fe 3000, CABA",
                  "telefono": "1199887766",
                  "email": "sushi.test@logired.com"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/comercios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Sushi Express Test")))
                .andExpect(jsonPath("$.email", is("sushi.test@logired.com")))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        comercioId = json.get("id").asLong();
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/comercios con token valido retorna lista con al menos 1 comercio")
    void listarComercios_conToken_devuelve200() throws Exception {
        assertNotNull(tokenJwt);

        mockMvc.perform(get("/api/comercios")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/comercios/{id} retorna el comercio correcto")
    void obtenerComercioPorId_conToken_devuelve200() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(comercioId);

        mockMvc.perform(get("/api/comercios/" + comercioId)
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comercioId.intValue())))
                .andExpect(jsonPath("$.nombre", is("Sushi Express Test")));
    }

    // ==================================================================
    // 4. PEDIDOS — CRUD con token JWT y verificacion de persistencia
    // ==================================================================

    @Test
    @Order(9)
    @DisplayName("POST /api/pedidos crea un pedido en estado CREADO (HTTP 201)")
    void crearPedido_conToken_devuelve201() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(comercioId);

        String body = String.format("""
                {
                  "comercioId": %d,
                  "direccionDestino": "Av. Rivadavia 5000, CABA"
                }
                """, comercioId);

        MvcResult result = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.comercioId", is(comercioId.intValue())))
                .andExpect(jsonPath("$.estado", is("CREADO")))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        pedidoId = json.get("id").asLong();
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/pedidos retorna lista con al menos 1 pedido")
    void listarPedidos_conToken_devuelve200() throws Exception {
        assertNotNull(tokenJwt);

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/pedidos/{id} retorna el pedido correcto")
    void obtenerPedidoPorId_conToken_devuelve200() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        mockMvc.perform(get("/api/pedidos/" + pedidoId)
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$.estado", is("CREADO")));
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/pedidos/comercio/{id} filtra pedidos por comercioId")
    void listarPedidosPorComercio_devuelve200() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(comercioId);

        mockMvc.perform(get("/api/pedidos/comercio/" + comercioId)
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].comercioId", is(comercioId.intValue())));
    }

    @Test
    @Order(13)
    @DisplayName("GET /api/pedidos/estado/CREADO filtra pedidos por estado")
    void listarPedidosPorEstado_devuelve200() throws Exception {
        assertNotNull(tokenJwt);

        mockMvc.perform(get("/api/pedidos/estado/CREADO")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("CREADO")));
    }

    @Test
    @Order(14)
    @DisplayName("PATCH /api/pedidos/{id}/estado actualiza a EN_CAMINO y persiste en BD")
    void actualizarEstadoPedido_persiste() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content("{\"estado\": \"EN_CAMINO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_CAMINO")));

        // Verificar persistencia directa en BD H2
        Pedido pedidoBd = pedidosRepository.findById(pedidoId).orElseThrow();
        assertEquals(EstadoPedido.EN_CAMINO, pedidoBd.getEstado(),
                "El estado debe persistir como EN_CAMINO en la base de datos H2");
    }

    @Test
    @Order(15)
    @DisplayName("DELETE /api/pedidos/{id}/cancelar cancela el pedido y persiste en BD")
    void cancelarPedido_persiste() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        mockMvc.perform(delete("/api/pedidos/" + pedidoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));

        // Verificar persistencia directa en BD H2
        Pedido pedidoBd = pedidosRepository.findById(pedidoId).orElseThrow();
        assertEquals(EstadoPedido.CANCELADO, pedidoBd.getEstado(),
                "El estado debe persistir como CANCELADO en la base de datos H2");
    }

    @Test
    @Order(16)
    @DisplayName("DELETE /api/pedidos/{id}/cancelar sobre pedido ENTREGADO debe retornar 409 Conflict")
    void cancelarPedidoEntregado_debeRetornarError() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(comercioId);

        // Crear un nuevo pedido
        String bodyCrear = String.format(
                "{\"comercioId\": %d, \"direccionDestino\": \"Test entregado\"}", comercioId);

        MvcResult crear = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content(bodyCrear))
                .andExpect(status().isCreated())
                .andReturn();

        Long idEntregado = objectMapper
                .readTree(crear.getResponse().getContentAsString())
                .get("id").asLong();

        // Llevar a estado ENTREGADO
        mockMvc.perform(patch("/api/pedidos/" + idEntregado + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenJwt)
                .content("{\"estado\": \"ENTREGADO\"}"))
                .andExpect(status().isOk());

        // Intentar cancelar — debe retornar 409 Conflict (IllegalStateException mapeada por GlobalExceptionHandler)
        mockMvc.perform(delete("/api/pedidos/" + idEntregado + "/cancelar")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("entregado")));
    }
}
