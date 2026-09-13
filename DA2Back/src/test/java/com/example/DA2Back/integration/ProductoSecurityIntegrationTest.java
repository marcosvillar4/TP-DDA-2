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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String comercioToken;
    private String repartidorToken;

    @BeforeEach
    void prepararUsuarios() throws Exception {
        asegurarUsuario("producto.comercio@test.com", "20111111", Rol.COMERCIO);
        asegurarUsuario("producto.repartidor@test.com", "20222222", Rol.REPARTIDOR);

        adminToken = login("admin@da2back.com", "admin123");
        comercioToken = login("producto.comercio@test.com", "test123");
        repartidorToken = login("producto.repartidor@test.com", "test123");
    }

    @Test
    @DisplayName("ADMIN puede consultar productos")
    void adminPuedeConsultarProductos() throws Exception {
        mockMvc.perform(get("/productos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("COMERCIO puede consultar productos")
    void comercioPuedeConsultarProductos() throws Exception {
        mockMvc.perform(get("/productos")
                        .header("Authorization", "Bearer " + comercioToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN puede ejecutar POST /productos sin ser bloqueado por seguridad")
    void adminPuedeCrearProductos() throws Exception {
        verificarPostProductoAutorizado(adminToken);
    }

    @Test
    @DisplayName("COMERCIO puede ejecutar POST /productos sin ser bloqueado por seguridad")
    void comercioPuedeCrearProductos() throws Exception {
        verificarPostProductoAutorizado(comercioToken);
    }

    @Test
    @DisplayName("REPARTIDOR no puede operar productos")
    void repartidorNoPuedeOperarProductos() throws Exception {
        mockMvc.perform(get("/productos")
                        .header("Authorization", "Bearer " + repartidorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + repartidorToken)
                        .content(productoBody()))
                .andExpect(status().isForbidden());
    }

    private void verificarPostProductoAutorizado(String token) throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(productoBody()))
                .andExpect(status().isNotFound());
    }

    private String productoBody() {
        return """
                {
                  "sku": "TEST-001",
                  "nombre": "Mouse Logitech M170",
                  "descripcion": "Mouse inalámbrico para uso administrativo",
                  "categoria": "Periféricos",
                  "comercioId": 999
                }
                """;
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

    @Test
    @DisplayName("ADMIN puede ejecutar PUT /productos/{id} sin ser bloqueado por seguridad")
    void adminPuedeActualizarProductos() throws Exception {
        String body = """
                {
                  "nombre": "Mouse Logitech M170",
                  "descripcion": "Mouse inalámbrico para uso administrativo",
                  "categoria": "Periféricos"
                }
                """;

        mockMvc.perform(put("/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN puede ejecutar PATCH de estado de Producto sin ser bloqueado por seguridad")
    void adminPuedeCambiarEstadoProductos() throws Exception {
        mockMvc.perform(patch("/productos/999/activar")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/productos/999/desactivar")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
