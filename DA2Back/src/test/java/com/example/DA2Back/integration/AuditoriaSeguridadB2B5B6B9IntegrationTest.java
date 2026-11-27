package com.example.DA2Back.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.negocio.ControlIntentosLoginService;
import com.example.DA2Back.Seguridad.negocio.JwtService;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.config.CorsConfig;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.InventarioRepository;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Suite de integración para los pendientes de seguridad de la Auditoría (Grupo B):
 *  - B2: CORS con orígenes explícitos (sin comodines con credenciales).
 *  - B5: Protección contra Mass Assignment (rol, estado, precio/costoEnvio, comercioId, id).
 *  - B6: Protección contra SQL Injection y XSS en entradas de texto.
 *  - B9: Enmascaramiento de emails y ausencia de datos sensibles en logs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaSeguridadB2B5B6B9IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ComercioRepository comercioRepository;
    @Autowired private DepositoRepository depositoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ItemInventarioRepository itemInventarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario usuarioComercio;
    private Comercio comercioPropio;
    private Comercio comercioAjeno;
    private Deposito depositoPropio;
    private Producto productoPropio;
    private String tokenComercio;

    @BeforeEach
    void prepararEscenario() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        usuarioComercio = usuarioRepository.save(Usuario.builder()
                .email("comercio.b5." + sufijo + "@logired.com")
                .password(passwordEncoder.encode("PasswordSegura123!"))
                .nombre("Comercio")
                .apellido("Propio")
                .DNI("41" + sufijo.substring(0, 6))
                .telefono("1144556677")
                .rol(Rol.COMERCIO)
                .estado(EstadoUsuario.VALIDADO)
                .build());

        comercioPropio = comercioRepository.save(Comercio.builder()
                .nombreComercial("Comercio Propio " + sufijo)
                .razonSocial("Comercio Propio SA " + sufijo)
                .direccion("Av. Corrientes 1234, CABA")
                .CUIT("30-" + sufijo + "-1")
                .telefono("1144556677")
                .email("contacto." + sufijo + "@propio.com")
                .usuarioId(usuarioComercio.getId())
                .build());

        Usuario otroUsuario = usuarioRepository.save(Usuario.builder()
                .email("comercio.ajeno." + sufijo + "@logired.com")
                .password(passwordEncoder.encode("PasswordSegura123!"))
                .nombre("Comercio")
                .apellido("Ajeno")
                .DNI("42" + sufijo.substring(0, 6))
                .telefono("1144556688")
                .rol(Rol.COMERCIO)
                .estado(EstadoUsuario.VALIDADO)
                .build());

        comercioAjeno = comercioRepository.save(Comercio.builder()
                .nombreComercial("Comercio Ajeno " + sufijo)
                .razonSocial("Comercio Ajeno SA " + sufijo)
                .direccion("Av. Santa Fe 4321, CABA")
                .CUIT("30-" + sufijo + "-2")
                .telefono("1144556688")
                .email("contacto." + sufijo + "@ajeno.com")
                .usuarioId(otroUsuario.getId())
                .build());

        Usuario usuarioDeposito = usuarioRepository.save(Usuario.builder()
                .email("deposito.b5." + sufijo + "@logired.com")
                .password(passwordEncoder.encode("PasswordSegura123!"))
                .nombre("Deposito")
                .apellido("Propio")
                .DNI("43" + sufijo.substring(0, 6))
                .telefono("1144556699")
                .rol(Rol.DEPOSITO)
                .estado(EstadoUsuario.VALIDADO)
                .build());

        depositoPropio = new Deposito();
        depositoPropio.setNombre("Deposito " + sufijo);
        depositoPropio.setDireccion("Castro Barros 450, CABA");
        depositoPropio.setComercio(comercioPropio);
        depositoPropio.setUsuarioId(usuarioDeposito.getId());
        depositoPropio = depositoRepository.save(depositoPropio);

        productoPropio = productoRepository.save(new Producto(
                null,
                "SKU-" + sufijo,
                "Zapatilla Segura " + sufijo,
                "Calzado deportivo",
                "Calzado",
                EstadoProducto.ACTIVO,
                comercioPropio
        ));

        Inventario inv = new Inventario();
        inv.setComercio(comercioPropio);
        inv = inventarioRepository.save(inv);

        ItemInventario item = new ItemInventario();
        item.setInventario(inv);
        item.setProducto(productoPropio);
        item.setDeposito(depositoPropio);
        item.setCantidad(20);
        itemInventarioRepository.save(item);

        tokenComercio = jwtService.generateToken(usuarioComercio);
    }

    // =========================================================================
    // B2 — CORS con orígenes explícitos
    // =========================================================================

    @Test
    @DisplayName("B2: Preflight OPTIONS desde origen explícito permitido responde 200 con credenciales y sin comodín")
    void b2_corsPermiteOrigenExplicitoEnPreflight() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("B2: Preflight OPTIONS desde un origen externo no autorizado es rechazado con 403")
    void b2_corsRechazaOrigenNoAutorizadoEnPreflight() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "https://atacante.malicioso.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("B2: CorsConfig rechaza orígenes con comodín (*) para impedir comodines con credenciales")
    void b2_corsConfigRechazaComodinesConCredenciales() {
        assertThrows(IllegalStateException.class,
                () -> CorsConfig.parsearYValidarOrigenes("http://localhost:*"));
        assertThrows(IllegalStateException.class,
                () -> CorsConfig.parsearYValidarOrigenes("http://192.168.*:*"));
        assertThrows(IllegalStateException.class,
                () -> CorsConfig.parsearYValidarOrigenes("   "));
    }

    // =========================================================================
    // B5 — Mass Assignment
    // =========================================================================

    @Test
    @DisplayName("B5: El registro público rechaza rol ADMIN e ignora id/estado forzados en el payload")
    void b5_registroRechazaRolAdminYIgnoraEstadoEIdForzados() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        // 1) Intento de registrarse como ADMIN -> rechazado (400 Bad Request)
        String payloadAdmin = """
                {
                  "rol": "ADMIN",
                  "email": "intento.admin.%s@logired.com",
                  "password": "PasswordSegura123!",
                  "nombre": "Hacker",
                  "apellido": "Admin",
                  "dni": "99%s",
                  "telefono": "1144556677"
                }
                """.formatted(sufijo, sufijo.substring(0, 6));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAdmin))
                .andExpect(status().isBadRequest());
        assertFalse(usuarioRepository.existsByEmail("intento.admin." + sufijo + "@logired.com"));

        // 2) Registro de REPARTIDOR inyectando estado=VALIDADO e id=999999 -> queda EN_EVALUACION y con ID propio
        String emailRep = "rep.mass." + sufijo + "@logired.com";
        int tresDigitos = Math.abs(sufijo.hashCode() % 900) + 100;
        String payloadRepartidor = """
                {
                  "id": 999999,
                  "estado": "VALIDADO",
                  "rol": "REPARTIDOR",
                  "email": "%s",
                  "password": "PasswordSegura123!",
                  "nombre": "Juan",
                  "apellido": "Moto",
                  "dni": "88%s",
                  "telefono": "1144556677",
                  "vehiculo": "Moto 150cc",
                  "patente": "AB%dCD",
                  "zona": "CABA"
                }
                """.formatted(emailRep, sufijo.substring(0, 6), tresDigitos);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRepartidor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_EVALUACION"));

        Usuario creado = usuarioRepository.findByEmail(emailRep).orElseThrow();
        assertEquals(EstadoUsuario.EN_EVALUACION, creado.getEstado(),
                "B5: un repartidor no puede auto-validarse por Mass Assignment");
        assertNotEquals(999999L, creado.getId(),
                "B5: el ID del usuario nunca debe tomarse del payload de registro");
    }

    @Test
    @DisplayName("B5: Crear pedido ignora id, estado, costoEnvio, precio y repartidorId inyectados en el JSON")
    void b5_crearPedidoIgnoraCamposProtegidos() throws Exception {
        String payloadPedido = """
                {
                  "id": 777777,
                  "comercioId": %d,
                  "depositoId": %d,
                  "direccionDestino": "Av. Rivadavia 2500, CABA",
                  "estado": "ENTREGADO",
                  "costoEnvio": 0.01,
                  "precio": 0.01,
                  "repartidorId": 1,
                  "detalles": [
                    {
                      "productoId": %d,
                      "cantidad": 2
                    }
                  ]
                }
                """.formatted(comercioPropio.getId(), depositoPropio.getId(), productoPropio.getId());

        MvcResult result = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + tokenComercio)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadPedido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotEquals(777777L, json.path("id").asLong(), "B5: el id del pedido no puede forzarse");
        assertTrue(json.path("repartidorId").isNull(), "B5: el repartidorId no puede forzarse al crear un pedido");
        BigDecimal costoCalculado = new BigDecimal(json.path("costoEnvio").asText());
        assertTrue(costoCalculado.compareTo(new BigDecimal("0.01")) > 0,
                "B5: el costoEnvio debe ser calculado por el servidor, nunca tomado del cliente");
    }

    @Test
    @DisplayName("B5: Actualizar producto ignora id, comercioId ajeno y estado inyectados en el payload")
    void b5_actualizarProductoIgnoraComercioIdEstadoEId() throws Exception {
        String payloadUpdate = """
                {
                  "id": 555555,
                  "comercioId": %d,
                  "estado": "INACTIVO",
                  "nombre": "Zapatilla Actualizada",
                  "descripcion": "Descripcion actualizada",
                  "categoria": "Calzado"
                }
                """.formatted(comercioAjeno.getId());

        mockMvc.perform(put("/productos/" + productoPropio.getId())
                        .header("Authorization", "Bearer " + tokenComercio)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoPropio.getId()))
                .andExpect(jsonPath("$.comercioId").value(comercioPropio.getId()))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        Producto enBase = productoRepository.findById(productoPropio.getId()).orElseThrow();
        assertEquals(comercioPropio.getId(), enBase.getComercio().getId(),
                "B5: PUT /productos/{id} no debe permitir reasignar el producto a otro comercio");
        assertEquals(EstadoProducto.ACTIVO, enBase.getEstado(),
                "B5: PUT /productos/{id} no debe cambiar el estado sin pasar por el endpoint específico");
    }

    // =========================================================================
    // B6 — SQL Injection y XSS
    // =========================================================================

    @Test
    @DisplayName("B6: Payloads de SQL Injection en login son rechazados con 401 sin alterar la tabla usuarios")
    void b6_sqliEnLoginEsRechazado() throws Exception {
        long totalAntes = usuarioRepository.count();
        String[] payloadsSqli = {
                "' OR '1'='1",
                "' OR 1=1 --",
                "admin@da2back.com' --",
                "'; DROP TABLE usuarios; --"
        };

        for (String sqli : payloadsSqli) {
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of("email", sqli, "password", "' OR '1'='1"));
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        assertEquals(totalAntes, usuarioRepository.count(),
                "B6: ningún intento de SQLi debe eliminar ni alterar registros de usuarios");
    }

    @Test
    @DisplayName("B6: Payloads de SQL Injection en filtros de búsqueda se tratan como literales y no filtran datos")
    void b6_sqliEnBusquedaDeProductosNoAlteraConsulta() throws Exception {
        long productosAntes = productoRepository.count();

        mockMvc.perform(get("/productos")
                        .header("Authorization", "Bearer " + tokenComercio)
                        .param("comercioId", String.valueOf(comercioPropio.getId()))
                        .param("nombre", "' OR 1=1 --")
                        .param("categoria", "'; DROP TABLE productos; --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        assertEquals(productosAntes, productoRepository.count(),
                "B6: la tabla productos debe permanecer intacta tras payloads SQLi");
    }

    @Test
    @DisplayName("B6: Entradas con tags XSS se devuelven como application/json seguro y nunca como text/html")
    void b6_xssEnCamposDeTextoSeDevuelveComoJsonNoEjecutable() throws Exception {
        String payloadXss = """
                {
                  "nombre": "<script>alert('xss')</script>",
                  "sku": "XSS-%s",
                  "descripcion": "<img src=x onerror=alert(1)>",
                  "categoria": "Calzado",
                  "comercioId": %d
                }
                """.formatted(UUID.randomUUID().toString().substring(0, 6), comercioPropio.getId());

        mockMvc.perform(post("/productos")
                        .header("Authorization", "Bearer " + tokenComercio)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadXss))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // =========================================================================
    // B9 — Logs sin PII ni secretos
    // =========================================================================

    @Test
    @DisplayName("B9: Los logs de intentos de login fallidos enmascaran el email y nunca registran la contraseña")
    void b9_logsDeLoginFallidoEnmascaranEmailYNoExponenPassword() throws Exception {
        String emailSensible = "victima.privada@logired.com";
        String passwordIntentada = "ClaveSecretaQueNoDebeLoguearse99!";

        Logger logger = (Logger) LoggerFactory.getLogger(ControlIntentosLoginService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Level prevLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);

        try {
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of("email", emailSensible, "password", passwordIntentada));
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());

            assertFalse(appender.list.isEmpty(), "Debe haberse registrado el evento de intento fallido");
            for (ILoggingEvent evento : appender.list) {
                String msg = evento.getFormattedMessage();
                assertFalse(msg.contains(emailSensible),
                        "B9: el log no debe contener el email completo en texto plano: " + msg);
                assertFalse(msg.contains(passwordIntentada),
                        "B9: el log jamás debe contener la contraseña enviada");
                assertTrue(msg.contains("v***@logired.com"),
                        "B9: el log debe mostrar el email enmascarado");
            }
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(prevLevel);
            appender.stop();
        }

        assertEquals("***", ControlIntentosLoginService.enmascararEmail(null));
        assertEquals("***", ControlIntentosLoginService.enmascararEmail("   "));
        assertEquals("***", ControlIntentosLoginService.enmascararEmail("sin-arroba"));
        assertEquals("a***@dominio.com", ControlIntentosLoginService.enmascararEmail("ana@dominio.com"));
    }
}
