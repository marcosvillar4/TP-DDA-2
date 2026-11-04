package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.negocio.UsuarioFactory;
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
 * Crea comercio y deposito reales de prueba para validar la coherencia del pedido.
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
    private UsuarioFactory usuarioFactory;

    @Autowired
    private PedidosRepository pedidosRepository;
    @Autowired private ComercioRepository comercios;
    @Autowired private DepositoRepository depositos;

    @Autowired
    private com.example.DA2Back.comercio.dato.ComercioRepository comercioRepository;

    @Autowired
    private com.example.DA2Back.deposito.dato.DepositoRepository depositoRepository;

    @Autowired
    private com.example.DA2Back.producto.dato.ProductoRepository productoRepository;

    @Autowired
    private com.example.DA2Back.inventario.dato.InventarioRepository inventarioRepository;

    @Autowired
    private com.example.DA2Back.inventario.dato.ItemInventarioRepository itemInventarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Estado compartido entre tests ordenados */
    private String tokenJwt;
    private Long pedidoId;
    private Long comercioId;
    private Long depositoId;
    private Long productoId;

    private static final String ADMIN_EMAIL = "admin@da2back.com";
    private static final String ADMIN_PASSWORD = "admin123";
    // ------------------------------------------------------------------
    // Semilla de datos (una vez antes de todos los tests)
    // ------------------------------------------------------------------

    @BeforeAll
    void sembrarDatos() {
        if (!usuarioRepository.existsByEmail(ADMIN_EMAIL)) {
            Usuario admin = usuarioFactory.crearAdministrador(
                    ADMIN_EMAIL,
                    ADMIN_PASSWORD,
                    "Administrador",
                    "LogiRed",
                    "00000000",
                    "0000000000"
            );
            usuarioRepository.save(admin);
        }

        Usuario usuarioComercio = usuarioRepository.findByEmail("comercio.test.integ@logired.com")
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .email("comercio.test.integ@logired.com")
                        .password("password123")
                        .nombre("Comercio")
                        .apellido("Test")
                        .DNI("40000001")
                        .telefono("1122334455")
                        .rol(Rol.COMERCIO)
                        .estado(com.example.DA2Back.Seguridad.dato.EstadoUsuario.VALIDADO)
                        .build()));

        com.example.DA2Back.comercio.dato.Comercio comercio = comercioRepository.findByUsuarioId(usuarioComercio.getId())
                .orElseGet(() -> comercioRepository.save(com.example.DA2Back.comercio.dato.Comercio.builder()
                        .nombreComercial("Comercio Test")
                        .razonSocial("Comercio Test SA")
                        .direccion("Calle 123")
                        .CUIT("30-12345678-9")
                        .telefono("1122334455")
                        .email("comercio.test.integ@logired.com")
                        .usuarioId(usuarioComercio.getId())
                        .build()));
        comercioId = comercio.getId();

        Usuario usuarioDeposito = usuarioRepository.findByEmail("deposito.test.integ@logired.com")
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .email("deposito.test.integ@logired.com")
                        .password("password123")
                        .nombre("Deposito")
                        .apellido("Test")
                        .DNI("40000002")
                        .telefono("1122334456")
                        .rol(Rol.DEPOSITO)
                        .estado(com.example.DA2Back.Seguridad.dato.EstadoUsuario.VALIDADO)
                        .build()));

        com.example.DA2Back.deposito.dato.Deposito deposito = depositoRepository.findByUsuarioId(usuarioDeposito.getId())
                .orElseGet(() -> {
                    com.example.DA2Back.deposito.dato.Deposito d = new com.example.DA2Back.deposito.dato.Deposito();
                    d.setNombre("Deposito Test");
                    d.setDireccion("Av Central 100");
                    d.setComercio(comercio);
                    d.setUsuarioId(usuarioDeposito.getId());
                    return depositoRepository.save(d);
                });
        depositoId = deposito.getId();

        com.example.DA2Back.inventario.dato.Inventario inventario = inventarioRepository.findByComercioId(comercio.getId())
                .orElseGet(() -> inventarioRepository.save(new com.example.DA2Back.inventario.dato.Inventario(null, comercio, new java.util.ArrayList<>())));

        com.example.DA2Back.producto.dato.Producto producto = productoRepository.findByComercioId(comercio.getId()).stream().findFirst()
                .orElseGet(() -> productoRepository.save(new com.example.DA2Back.producto.dato.Producto(null, "SKU-TEST-1", "Prod Test", "Desc", "General", com.example.DA2Back.producto.dato.EstadoProducto.ACTIVO, comercio)));
        productoId = producto.getId();

        if (itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(inventario.getId(), producto.getId(), deposito.getId()).isEmpty()) {
            itemInventarioRepository.save(new com.example.DA2Back.inventario.dato.ItemInventario(null, inventario, producto, deposito, 100, 5));
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
                  "email": "admin@da2back.com",
                  "password": "admin123"
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
                        .content("{\"email\":\"admin@da2back.com\",\"password\":\"mal\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ==================================================================
    // 3. PEDIDOS — CRUD con token JWT
    // ==================================================================

    @Test
    @Order(5)
    @DisplayName("POST /api/pedidos crea un pedido en estado PENDIENTE_COTIZACION (HTTP 201)")
    void crearPedido_conToken_devuelve201() throws Exception {
        assertNotNull(tokenJwt, "Requiere token — ejecutar test de login primero");

        String body = String.format("""
                {
                  "comercioId": %d,
                  "depositoId": %d,
"direccionDestino": "Av. Rivadavia 5000, CABA",
                  "detalles": [
                    {
                      "productoId": %d,
                      "cantidad": 2
                    }
                  ]
                }
                """, comercioId, depositoId, productoId);

        MvcResult result = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenJwt)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.comercioId", is(comercioId.intValue())))
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

        mockMvc.perform(get("/api/pedidos/comercio/" + comercioId)
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comercioId", is(comercioId.intValue())));
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
    @DisplayName("PATCH /api/pedidos/{id}/cancelar cancela el pedido y persiste en BD")
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
    @Order(11)
    @DisplayName("DELETE /api/pedidos/{id}/cancelar sobre CANCELADO devuelve 409 Conflict")
    void cancelarEntregado_devuelve409() throws Exception {
        assertNotNull(tokenJwt);
        assertNotNull(pedidoId);

        // Intentar cancelar de nuevo -> debe retornar 409
        mockMvc.perform(delete("/api/pedidos/" + pedidoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isConflict());
    }
}

