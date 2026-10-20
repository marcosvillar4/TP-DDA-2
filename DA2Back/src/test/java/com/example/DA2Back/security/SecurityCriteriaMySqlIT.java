package com.example.DA2Back.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.DA2Back.Da2BackApplication;
import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.dto.RegistroComercioDTO;
import com.example.DA2Back.Seguridad.dto.RegistroDepositoDTO;
import com.example.DA2Back.Seguridad.negocio.AdminSeeder;
import com.example.DA2Back.Seguridad.negocio.IUsuarioService;
import com.example.DA2Back.Seguridad.negocio.UsuarioFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Opt-in MySQL tests: the IT suffix excludes these from the default Surefire suite.
 * Run explicitly with -Dtest=SecurityCriteriaMySqlIT. All fixture rows roll back.
 */
@SpringBootTest(classes = Da2BackApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(printOnlyOnFailure = false, print = org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint.NONE)
@ActiveProfiles("security-mysql")
@Import(SecurityCriteriaMySqlIT.IsolatedMySql.class)
@Transactional
class SecurityCriteriaMySqlIT {
    private static final String SCHEMA = "marketplace_security_test";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/" + SCHEMA;
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired private UsuarioRepository usuarios;
    @Autowired private UsuarioFactory factory;
    @Autowired private IUsuarioService usuarioService;
    @Autowired private PasswordEncoder encoder;
    @Autowired private EntityManager em;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mvc;
    @Autowired private Validator validator;

    // Suppress startup seeding; W04 invokes the real implementation explicitly.
    @MockitoBean private AdminSeeder startupSeeder;

    @DynamicPropertySource
    static void isolatedProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> URL);
        properties.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        properties.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        properties.add("spring.jpa.show-sql", () -> "false");
        properties.add("spring.sql.init.mode", () -> "never");
        properties.add("app.mensajeria.habilitada", () -> "false");
        properties.add("app.mensajeria.broker-embebido", () -> "false");
        properties.add("spring.jms.listener.auto-startup", () -> "false");
        properties.add("spring.mail.host", () -> "false");
        properties.add("spring.mail.test-connection", () -> "false");
        properties.add("logging.level.org.hibernate.SQL", () -> "OFF");
        properties.add("logging.level.org.hibernate.orm.jdbc.bind", () -> "OFF");
        properties.add("logging.level.org.springframework.test.util.ReflectionTestUtils", () -> "OFF");
        properties.add("jwt.secret", () -> "security-mysql-fictional-signing-key-only-for-tests");
        properties.add("jwt.expiration", () -> "60000");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class IsolatedMySql {
        @Bean
        DataSource dataSource(Environment environment) throws Exception {
            if (!Arrays.equals(environment.getActiveProfiles(), new String[]{"security-mysql"})) {
                throw new IllegalStateException("Only the security-mysql profile is permitted");
            }
            var dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(URL);
            dataSource.setUsername(requiredVariable("SECURITY_TEST_DB_USERNAME"));
            dataSource.setPassword(requiredVariable("SECURITY_TEST_DB_PASSWORD"));
            // This bean must pass the check before Hibernate receives a DataSource.
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT DATABASE()")) {
                if (!result.next() || !SCHEMA.equals(result.getString(1))
                        || !"MySQL".equals(connection.getMetaData().getDatabaseProductName())) {
                    throw new IllegalStateException("Refusing a database other than the isolated MySQL schema");
                }
            }
            return dataSource;
        }

        private static String requiredVariable(String name) {
            String value = System.getenv(name);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Missing local environment variable: " + name);
            }
            return value;
        }
    }

    @Test
    void secW04_bcryptYLogsSinContrasenas() throws Exception {
        assertTrue(usuarios.findAll().stream().noneMatch(u -> u.getRol() == Rol.ADMIN),
                "W04 requires an isolated schema without an existing administrator");
        String adminPassword = UUID.randomUUID().toString();
        String registrationPassword = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        String adminEmail = "admin-" + id + "@security.invalid";

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger seederLogger = (Logger) LoggerFactory.getLogger(AdminSeeder.class);
        var capture = new ListAppender<ILoggingEvent>();
        capture.start();
        Level previousLevel = seederLogger.getLevel();
        boolean previousAdditivity = seederLogger.isAdditive();
        root.addAppender(capture);
        seederLogger.addAppender(capture);
        // Capture the vulnerable message without forwarding it to console/Surefire.
        seederLogger.setAdditive(false);
        seederLogger.setLevel(Level.INFO);
        try {
            var seeder = new AdminSeeder(usuarios, factory);
            ReflectionTestUtils.setField(seeder, "adminEmail", adminEmail);
            ReflectionTestUtils.setField(seeder, "adminPassword", adminPassword);
            ReflectionTestUtils.setField(seeder, "adminNombre", "Ficticio");
            ReflectionTestUtils.setField(seeder, "adminApellido", "Seguridad");
            ReflectionTestUtils.setField(seeder, "adminDni", "admin-" + id);
            ReflectionTestUtils.setField(seeder, "adminTelefono", "0000000000");
            seeder.run();

            var registration = new RegistroComercioDTO();
            registration.setEmail("comercio-" + id + "@security.invalid");
            registration.setPassword(registrationPassword);
            registration.setNombre("Ficticio");
            registration.setApellido("Seguridad");
            registration.setDni("comercio-" + id);
            registration.setTelefono("0000000000");
            registration.setRol(Rol.COMERCIO);
            ReflectionTestUtils.setField(registration, "nombreComercial", "Comercio ficticio");
            ReflectionTestUtils.setField(registration, "razonSocial", "Prueba de seguridad");
            ReflectionTestUtils.setField(registration, "cuit", id);
            ReflectionTestUtils.setField(registration, "direccion", "Direccion ficticia");
            assertTrue(validator.validate(registration).isEmpty(), "Registration fixture must be valid");
            usuarioService.registrar(registration);
            em.flush();
            em.clear();

            String adminHash = jdbc.queryForObject("SELECT password FROM usuarios WHERE email = ?", String.class, adminEmail);
            String userHash = jdbc.queryForObject("SELECT password FROM usuarios WHERE email = ?", String.class, registration.getEmail());
            boolean exposed = capture.list.stream().map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains(adminPassword) || message.contains(registrationPassword));
            assertAll(
                    () -> assertInstanceOf(BCryptPasswordEncoder.class, encoder),
                    () -> verifyHash(adminPassword, adminHash),
                    () -> verifyHash(registrationPassword, userHash),
                    () -> assertFalse(encoder.encode(registrationPassword).equals(encoder.encode(registrationPassword)),
                            "BCrypt must generate a different salt for each encoding"),
                    () -> assertFalse(exposed, "SEC-W04: una contrasena aparecio en logs (valor omitido)")
            );
        } finally {
            seederLogger.detachAppender(capture);
            root.detachAppender(capture);
            seederLogger.setLevel(previousLevel);
            seederLogger.setAdditive(previousAdditivity);
            capture.stop();
        }
    }

    @Test
    void secW05_repartidorNoCreaDepositos() throws Exception {
        String id = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        Usuario actor = usuarios.saveAndFlush(Usuario.builder()
                .email("repartidor-" + id + "@security.invalid").password(encoder.encode(password))
                .nombre("Ficticio").apellido("Seguridad").DNI(id).telefono("0000000000")
                .rol(Rol.REPARTIDOR).estado(EstadoUsuario.VALIDADO).build());
        var login = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(Map.of("email", actor.getEmail(), "password", password))))
                .andReturn().getResponse();
        assertEquals(200, login.getStatus(), "The real login must authenticate the fixture");
        String jwt = JSON.readTree(login.getContentAsString()).path("token").asText();
        assertFalse(jwt.isBlank(), "The real login must return a JWT");

        Map<String, String> body = Map.of(
                "email", "encargado-" + id + "@security.invalid",
                "password", UUID.randomUUID().toString(),
                "nombre", "Ficticio", "apellido", "Seguridad",
                "dni", "encargado-" + id, "telefono", "0000000000",
                "nombreDeposito", "Deposito ficticio", "direccionDeposito", "Direccion ficticia");
        String request = JSON.writeValueAsString(body);
        assertTrue(validator.validate(JSON.readValue(request, RegistroDepositoDTO.class)).isEmpty(),
                "The rejected request must have a valid body");
        var before = snapshot();
        var response = mvc.perform(post("/depositos").header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andReturn().getResponse();
        em.flush();
        var after = snapshot();
        assertAll(
                () -> assertEquals(403, response.getStatus(), "SEC-W05 requires HTTP 403"),
                () -> assertTrue(before.equals(after), "All table contents must remain unchanged"),
                () -> assertFalse(usuarios.existsByEmail(body.get("email")), "No responsible user may be created")
        );
    }

    private void verifyHash(String original, String stored) {
        assertAll(
                () -> assertTrue(stored != null && stored.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}"),
                        "The stored value must have BCrypt format"),
                () -> assertFalse(original.equals(stored), "No plaintext password may be persisted"),
                () -> assertTrue(encoder.matches(original, stored), "The correct password must match"),
                () -> assertFalse(encoder.matches(original + "-incorrect", stored), "An incorrect password must not match")
        );
    }

    private Map<String, String> snapshot() throws Exception {
        var result = new LinkedHashMap<String, String>();
        for (String table : jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name",
                String.class, SCHEMA)) {
            if (!table.matches("[a-z_]+")) {
                throw new IllegalStateException("Unexpected table name in isolated schema");
            }
            byte[] rows = JSON.writeValueAsBytes(jdbc.queryForList("SELECT * FROM `" + table + "` ORDER BY id"));
            result.put(table, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(rows)));
        }
        return result;
    }
}
