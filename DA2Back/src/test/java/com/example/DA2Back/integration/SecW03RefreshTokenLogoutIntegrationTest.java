package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SEC-W03 (OWASP A07:2025 / ASVS 5.0):
 * Tras logout, el refresh token queda revocado y no emite nuevos tokens.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecW03RefreshTokenLogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMAIL = "secw03.usuario@logired.com";
    private static final String PASSWORD = "PasswordSegura123!";

    @BeforeEach
    void prepararUsuario() {
        if (!usuarioRepository.existsByEmail(EMAIL)) {
            usuarioRepository.save(Usuario.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .nombre("Nicolás")
                    .apellido("SecW03")
                    .DNI("39888777")
                    .telefono("1144556677")
                    .rol(Rol.COMERCIO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());
        }
    }

    @Test
    @DisplayName("SEC-W03: Tras POST /logout, intentar renovar con el refresh token anterior devuelve 401 Unauthorized")
    void secW03_trasLogoutRefreshTokenQuedaRevocadoYDevuelve401() throws Exception {
        // 1. DADO: Un usuario autenticado con una sesión activa (login emite token + refreshToken)
        JsonNode loginJson = hacerLogin();
        String accessTokenInicial = loginJson.get("token").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        assertNotNull(accessTokenInicial, "SEC-W03: El login debe emitir un access token JWT");
        assertNotNull(refreshToken, "SEC-W03: El login debe emitir un refresh token");
        assertFalse(refreshToken.isBlank(), "SEC-W03: El refresh token no debe estar vacío");

        String payloadRefresh = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        // 2. CUANDO: Ejecuta la acción de cierre de sesión (POST /logout)
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRefresh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente. El refresh token ha sido revocado."));

        // 3. ENTONCES: Intenta renovar token con el refresh token anterior y recibe 401 Unauthorized
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRefresh))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token inválido, revocado o vencido"));
    }

    @Test
    @DisplayName("SEC-W03: Tras POST /auth/logout, el endpoint /refresh también rechaza con 401 Unauthorized")
    void secW03_trasAuthLogoutRefreshTokenQuedaRevocado() throws Exception {
        JsonNode loginJson = hacerLogin();
        String refreshToken = loginJson.get("refreshToken").asText();

        String payloadRefresh = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRefresh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente. El refresh token ha sido revocado."));

        mockMvc.perform(post("/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRefresh))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("SEC-W03: Un refresh token válido emite nuevos tokens y rota (revoca) el refresh token usado")
    void secW03_rotacionDeRefreshTokenRevocaElAnterior() throws Exception {
        JsonNode loginJson = hacerLogin();
        String refreshTokenOriginal = loginJson.get("refreshToken").asText();

        String payloadOriginal = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshTokenOriginal);

        // Primer refresh con token activo -> 200 OK y entrega nuevo refreshToken
        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadOriginal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String nuevoRefreshToken = refreshJson.get("refreshToken").asText();
        assertNotEquals(refreshTokenOriginal, nuevoRefreshToken,
                "SEC-W03: La rotación debe emitir un refresh token distinto al anterior");

        // Reutilizar el refresh token ya rotado debe ser rechazado con 401
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadOriginal))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode hacerLogin() throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
