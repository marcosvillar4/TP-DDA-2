package com.example.DA2Back.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;

/**
 * SEC-W01 (OWASP A07:2025 / ASVS 5.0):
 * Se bloquea la cuenta tras X intentos de login fallidos (X = 5 en los tests).
 *
 * Cada test usa su propio usuario para no compartir el contador en memoria.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecW01BloqueoPorIntentosFallidosIntegrationTest {

    private static final int MAX_INTENTOS = 5;
    private static final String PASSWORD = "PasswordSegura123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("SEC-W01: Tras X intentos fallidos la cuenta queda BLOQUEADA y el login correcto ya no funciona")
    void secW01_cuentaSeBloqueaTrasXIntentosFallidos() throws Exception {
        String email = "secw01.bloqueo@logired.com";
        crearUsuario(email, "39000001", EstadoUsuario.VALIDADO);

        // Los primeros X-1 intentos fallan con 401 y la cuenta sigue activa
        for (int i = 1; i < MAX_INTENTOS; i++) {
            login(email, "incorrecta-" + i)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Email o contraseña incorrectos"));
            assertEquals(EstadoUsuario.VALIDADO, estadoDe(email),
                    "La cuenta no debe bloquearse antes del intento " + MAX_INTENTOS);
        }

        // El intento X bloquea la cuenta
        login(email, "incorrecta-final")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("La cuenta está bloqueada por intentos fallidos consecutivos"));
        assertEquals(EstadoUsuario.BLOQUEADO, estadoDe(email),
                "SEC-W01: tras " + MAX_INTENTOS + " intentos fallidos la cuenta debe quedar BLOQUEADA");

        // Aun con la contraseña correcta, la cuenta bloqueada no puede iniciar sesión
        login(email, PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("La cuenta está bloqueada y no se puede iniciar sesión"));
    }

    @Test
    @DisplayName("SEC-W01: Un login exitoso antes del umbral reinicia el contador de intentos fallidos")
    void secW01_loginExitosoReiniciaElContador() throws Exception {
        String email = "secw01.reinicio@logired.com";
        crearUsuario(email, "39000002", EstadoUsuario.VALIDADO);

        for (int i = 1; i < MAX_INTENTOS; i++) {
            login(email, "incorrecta-" + i).andExpect(status().isUnauthorized());
        }

        // Login correcto con X-1 fallos acumulados: entra y reinicia el contador
        login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // X-1 nuevos fallos no alcanzan para bloquear (el contador volvió a cero)
        for (int i = 1; i < MAX_INTENTOS; i++) {
            login(email, "incorrecta-bis-" + i).andExpect(status().isUnauthorized());
        }
        assertEquals(EstadoUsuario.VALIDADO, estadoDe(email),
                "Los fallos previos al login exitoso no deben sumarse a los posteriores");

        login(email, PASSWORD).andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-W01: Los intentos fallidos de un usuario no bloquean a otro")
    void secW01_elContadorEsPorUsuario() throws Exception {
        String atacado = "secw01.atacado@logired.com";
        String otro = "secw01.otro@logired.com";
        crearUsuario(atacado, "39000003", EstadoUsuario.VALIDADO);
        crearUsuario(otro, "39000004", EstadoUsuario.VALIDADO);

        for (int i = 1; i <= MAX_INTENTOS; i++) {
            login(atacado, "incorrecta-" + i).andExpect(status().isUnauthorized());
        }

        assertEquals(EstadoUsuario.BLOQUEADO, estadoDe(atacado));
        assertEquals(EstadoUsuario.VALIDADO, estadoDe(otro));
        login(otro, PASSWORD).andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-W01: El email se normaliza (mayúsculas/espacios) al contar intentos fallidos")
    void secW01_emailSeNormalizaAlContarIntentos() throws Exception {
        String email = "secw01.normaliza@logired.com";
        crearUsuario(email, "39000005", EstadoUsuario.VALIDADO);

        // 3 fallos con el email exacto + 2 con variantes de mayúsculas/espacios: todos suman al mismo contador
        login(email, "incorrecta").andExpect(status().isUnauthorized());
        login(email, "incorrecta").andExpect(status().isUnauthorized());
        login(email, "incorrecta").andExpect(status().isUnauthorized());
        assertEquals(EstadoUsuario.VALIDADO, estadoDe(email));

        login(" " + email.toUpperCase() + " ", "incorrecta").andExpect(status().isUnauthorized());
        login(email.toUpperCase(), "incorrecta").andExpect(status().isUnauthorized());

        assertEquals(EstadoUsuario.BLOQUEADO, estadoDe(email));
    }

    @Test
    @DisplayName("SEC-W01: Una cuenta ya BLOQUEADA no suma intentos y responde 401")
    void secW01_cuentaYaBloqueadaRechazaElLogin() throws Exception {
        String email = "secw01.yabloqueada@logired.com";
        crearUsuario(email, "39000006", EstadoUsuario.BLOQUEADO);

        login(email, PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("La cuenta está bloqueada y no se puede iniciar sesión"));
        login(email, "incorrecta")
                .andExpect(status().isUnauthorized());

        assertEquals(EstadoUsuario.BLOQUEADO, estadoDe(email));
    }

    // ------------------------------------------------------------------ helpers

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void crearUsuario(String email, String dni, EstadoUsuario estado) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .nombre("Sec")
                .apellido("W01")
                .DNI(dni)
                .telefono("1144556677")
                .rol(Rol.COMERCIO)
                .estado(estado)
                .build());
    }

    private EstadoUsuario estadoDe(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow().getEstado();
    }
}
