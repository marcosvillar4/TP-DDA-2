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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepositoSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String comercioToken;

    @BeforeEach
    void prepararUsuarios() throws Exception {
        asegurarUsuario("deposito.security.comercio@test.com", "30111111", Rol.COMERCIO);

        adminToken = login("admin@da2back.com", "admin123");
        comercioToken = login("deposito.security.comercio@test.com", "test123");
    }

    @Test
    @DisplayName("ADMIN puede consultar depositos")
    void adminPuedeConsultarDepositos() throws Exception {
        mockMvc.perform(get("/depositos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuario autenticado no ADMIN no puede consultar depositos")
    void usuarioNoAdminNoPuedeConsultarDepositos() throws Exception {
        mockMvc.perform(get("/depositos")
                        .header("Authorization", "Bearer " + comercioToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario sin autenticar no puede consultar depositos")
    void usuarioSinAutenticarNoPuedeConsultarDepositos() throws Exception {
        mockMvc.perform(get("/depositos"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @DisplayName("Usuario autenticado no ADMIN no puede modificar depositos")
    void usuarioNoAdminNoPuedeModificarDepositos() throws Exception {
        String body = """
                {
                  "nombre": "Deposito Test",
                  "direccion": "Av. Test 123"
                }
                """;

        mockMvc.perform(put("/depositos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + comercioToken)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private void asegurarUsuario(String email, String dni, Rol rol) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }

        Usuario usuario = Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode("test123"))
                .nombre("Usuario")
                .apellido(rol.name())
                .DNI(dni)
                .telefono("1100000000")
                .rol(rol)
                .estado(EstadoUsuario.VALIDADO)
                .build();

        usuarioRepository.save(usuario);
    }
}
