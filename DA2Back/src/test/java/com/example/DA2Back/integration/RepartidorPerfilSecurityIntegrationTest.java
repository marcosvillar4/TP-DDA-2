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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RepartidorPerfilSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String comercioToken;
    private String depositoToken;
    private String repartidorToken;

    @BeforeEach
    void prepararUsuarios() throws Exception {
        asegurarUsuario("repartidor.perfil.comercio@test.com", "40111111", Rol.COMERCIO);
        asegurarUsuario("repartidor.perfil.deposito@test.com", "40222222", Rol.DEPOSITO);
        asegurarUsuario("repartidor.perfil.repartidor@test.com", "40333333", Rol.REPARTIDOR);

        adminToken = login("admin@da2back.com", "admin123");
        comercioToken = login("repartidor.perfil.comercio@test.com", "test123");
        depositoToken = login("repartidor.perfil.deposito@test.com", "test123");
        repartidorToken = login("repartidor.perfil.repartidor@test.com", "test123");
    }

    @Test
    @DisplayName("ADMIN no puede consultar pedidos disponibles del perfil repartidor")
    void adminNoPuedeConsultarPedidosDisponiblesDelPerfilRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/me/pedidos-disponibles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COMERCIO no puede consultar pedidos disponibles del perfil repartidor")
    void comercioNoPuedeConsultarPedidosDisponiblesDelPerfilRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/me/pedidos-disponibles")
                        .header("Authorization", "Bearer " + comercioToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DEPOSITO no puede consultar pedidos disponibles del perfil repartidor")
    void depositoNoPuedeConsultarPedidosDisponiblesDelPerfilRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/me/pedidos-disponibles")
                        .header("Authorization", "Bearer " + depositoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario sin autenticar no puede consultar pedidos disponibles del perfil repartidor")
    void usuarioSinAutenticarNoPuedeConsultarPedidosDisponiblesDelPerfilRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/me/pedidos-disponibles"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @DisplayName("REPARTIDOR autenticado llega al endpoint y recibe error controlado si no tiene perfil")
    void repartidorSinPerfilRecibeNotFoundControlado() throws Exception {
        mockMvc.perform(get("/repartidor/me/pedidos-disponibles")
                        .header("Authorization", "Bearer " + repartidorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN no puede tomar pedidos desde el perfil repartidor")
    void adminNoPuedeTomarPedidoDesdePerfilRepartidor() throws Exception {
        mockMvc.perform(patch("/repartidor/me/pedidos/1/tomar")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COMERCIO no puede tomar pedidos desde el perfil repartidor")
    void comercioNoPuedeTomarPedidoDesdePerfilRepartidor() throws Exception {
        mockMvc.perform(patch("/repartidor/me/pedidos/1/tomar")
                        .header("Authorization", "Bearer " + comercioToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DEPOSITO no puede tomar pedidos desde el perfil repartidor")
    void depositoNoPuedeTomarPedidoDesdePerfilRepartidor() throws Exception {
        mockMvc.perform(patch("/repartidor/me/pedidos/1/tomar")
                        .header("Authorization", "Bearer " + depositoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario sin autenticar no puede tomar pedidos desde el perfil repartidor")
    void usuarioSinAutenticarNoPuedeTomarPedidoDesdePerfilRepartidor() throws Exception {
        mockMvc.perform(patch("/repartidor/me/pedidos/1/tomar"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @DisplayName("REPARTIDOR autenticado llega al endpoint tomar y recibe error controlado si no tiene perfil")
    void repartidorSinPerfilNoPuedeTomarPedido() throws Exception {
        mockMvc.perform(patch("/repartidor/me/pedidos/1/tomar")
                        .header("Authorization", "Bearer " + repartidorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("REPARTIDOR no puede usar endpoint administrativo de asignación con repartidorId arbitrario")
    void repartidorNoPuedeUsarAsignacionAdministrativa() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/asignar/99")
                        .header("Authorization", "Bearer " + repartidorToken))
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
