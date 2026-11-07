package com.example.DA2Back.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * SEC-W02 (OWASP A07:2025 / ASVS 5.0):
 * El access token expira en <= 15 minutos; un token vencido, alterado
 * o con alg "none" recibe 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecW02TokenAccesoIntegrationTest {

    private static final String EMAIL = "secw02.usuario@logired.com";
    private static final String PASSWORD = "PasswordSegura123!";
    private static final String ENDPOINT_PROTEGIDO = "/usuarios/me";
    private static final String ENDPOINT_ADMIN = "/usuarios/admin/all";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void prepararUsuario() {
        if (!usuarioRepository.existsByEmail(EMAIL)) {
            usuarioRepository.save(Usuario.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .nombre("Sec")
                    .apellido("W02")
                    .DNI("39222111")
                    .telefono("1144556677")
                    .rol(Rol.COMERCIO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());
        }
    }

    // ---------------------------------------------------------------- vigencia

    @Test
    @DisplayName("SEC-W02: El access token emitido en el login expira en 15 minutos o menos")
    void secW02_accessTokenExpiraEnMenosDe15Minutos() throws Exception {
        String token = loginYObtenerToken();

        JsonNode payload = decodificarPayload(token);
        long iat = payload.get("iat").asLong();
        long exp = payload.get("exp").asLong();

        assertTrue(exp > iat, "SEC-W02: el token debe tener una expiración posterior a su emisión");
        assertTrue(exp - iat <= 15 * 60,
                "SEC-W02: el access token debe vivir como máximo 900 s, pero vive " + (exp - iat) + " s");
    }

    @Test
    @DisplayName("SEC-W02: Un token vigente es aceptado en un endpoint protegido")
    void secW02_tokenVigenteEsAceptado() throws Exception {
        mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer " + loginYObtenerToken()))
                .andExpect(status().isOk());
    }

    // ----------------------------------------------------------- rechazos 401

    @Test
    @DisplayName("SEC-W02: Un token vencido recibe 401 Unauthorized")
    void secW02_tokenVencidoRecibe401() throws Exception {
        String vencido = construirToken(EMAIL, Rol.COMERCIO, claveCorrecta(),
                new Date(System.currentTimeMillis() - 20 * 60_000L),
                new Date(System.currentTimeMillis() - 5 * 60_000L));

        mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Token de acceso vencido"));
    }

    @Test
    @DisplayName("SEC-W02: Un token con el payload alterado (escalada de rol) recibe 401 Unauthorized")
    void secW02_tokenConPayloadAlteradoRecibe401() throws Exception {
        String original = loginYObtenerToken();
        String[] partes = original.split("\\.");

        JsonNode payload = decodificarPayload(original);
        String payloadAlterado = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
                .replace(payload.get("rol").asText(), "ROLE_ADMIN");
        assertNotEquals(new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8), payloadAlterado,
                "el test debe haber modificado realmente el payload");

        String alterado = partes[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadAlterado.getBytes(StandardCharsets.UTF_8))
                + "." + partes[2];

        mockMvc.perform(get(ENDPOINT_ADMIN).header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token de acceso inválido"));
    }

    @Test
    @DisplayName("SEC-W02: Un token con la firma alterada recibe 401 Unauthorized")
    void secW02_tokenConFirmaAlteradaRecibe401() throws Exception {
        String original = loginYObtenerToken();
        String[] partes = original.split("\\.");

        // Se cambia un carácter del medio de la firma (el último puede ser solo relleno de bits).
        char[] firma = partes[2].toCharArray();
        int medio = firma.length / 2;
        firma[medio] = firma[medio] == 'A' ? 'B' : 'A';
        String alterado = partes[0] + "." + partes[1] + "." + new String(firma);

        mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-W02: Un token firmado con otra clave recibe 401 Unauthorized")
    void secW02_tokenFirmadoConOtraClaveRecibe401() throws Exception {
        SecretKey otraClave = Keys.hmacShaKeyFor(
                "OtraClaveDistintaDeAlMenos32Caracteres-123456".getBytes(StandardCharsets.UTF_8));
        String falso = construirToken(EMAIL, Rol.COMERCIO, otraClave,
                new Date(), new Date(System.currentTimeMillis() + 5 * 60_000L));

        mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer " + falso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-W02: Un token con alg \"none\" (sin firma) recibe 401 Unauthorized")
    void secW02_tokenConAlgNoneRecibe401() throws Exception {
        String header = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        long ahora = System.currentTimeMillis() / 1000;
        String payload = base64Url("{\"sub\":\"" + EMAIL + "\",\"rol\":\"ROLE_COMERCIO\","
                + "\"iat\":" + ahora + ",\"exp\":" + (ahora + 600) + "}");

        // Variantes habituales del ataque: sin firma, y con distintas grafías de "none".
        String[] ataques = {
                header + "." + payload + ".",
                base64Url("{\"alg\":\"None\",\"typ\":\"JWT\"}") + "." + payload + ".",
                base64Url("{\"alg\":\"NONE\",\"typ\":\"JWT\"}") + "." + payload + ".",
                header + "." + payload + "." + "firmaFalsa"
        };

        for (String ataque : ataques) {
            mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer " + ataque))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("SEC-W02: Un token malformado recibe 401 Unauthorized")
    void secW02_tokenMalformadoRecibe401() throws Exception {
        mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer esto.no.es-un-jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(ENDPOINT_PROTEGIDO).header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-W02: Sin token se recibe 401 Unauthorized en un endpoint protegido")
    void secW02_sinTokenRecibe401() throws Exception {
        mockMvc.perform(get(ENDPOINT_PROTEGIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Se requiere autenticación"));
    }

    @Test
    @DisplayName("SEC-W02: Un token válido sin permisos sigue recibiendo 403 (no se confunde con 401)")
    void secW02_tokenValidoSinPermisosSigueSiendo403() throws Exception {
        mockMvc.perform(get(ENDPOINT_ADMIN).header("Authorization", "Bearer " + loginYObtenerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-W02: Un token vencido en el header no impide hacer login (endpoint público)")
    void secW02_tokenVencidoNoBloqueaEndpointsPublicos() throws Exception {
        String vencido = construirToken(EMAIL, Rol.COMERCIO, claveCorrecta(),
                new Date(System.currentTimeMillis() - 20 * 60_000L),
                new Date(System.currentTimeMillis() - 5 * 60_000L));

        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Bearer " + vencido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ helpers

    private String loginYObtenerToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private SecretKey claveCorrecta() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private String construirToken(String email, Rol rol, SecretKey clave, Date emitido, Date vence) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", "ROLE_" + rol.name())
                .issuedAt(emitido)
                .expiration(vence)
                .signWith(clave)
                .compact();
    }

    private JsonNode decodificarPayload(String token) throws Exception {
        byte[] json = Base64.getUrlDecoder().decode(token.split("\\.")[1]);
        return objectMapper.readTree(json);
    }

    private String base64Url(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }
}
