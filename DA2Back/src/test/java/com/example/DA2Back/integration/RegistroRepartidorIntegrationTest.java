package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.*;
import com.example.DA2Back.Seguridad.negocio.JwtService;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import com.example.DA2Back.pedidos.dato.*;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.repartidor.dato.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Transacciones reales por solicitud, sin base compartida ni servicios externos.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:registrorepartidor;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.mail.host=false",
        "app.mensajeria.habilitada=false", "app.mapas.proveedor=local"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
class RegistroRepartidorIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired RepartidorRepository repartidores;
    @Autowired PedidosRepository pedidos;
    @Autowired ServicioDePedidos servicioPedidos;
    @Autowired JwtService jwt;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @MockitoBean NotificacionService notificaciones;

    private String email() { return UUID.randomUUID() + "@registro.invalid"; }
    private String patente() { return "AA" + String.format("%03d", repartidores.count() % 1000) + "ZZ"; }
    private String cuerpo(String email, String patente) {
        return """
                {"rol":"REPARTIDOR","email":"%s","password":"ClaveFicticia123!",
                "nombre":"Prueba","apellido":"Registro","dni":"%s","telefono":"000",
                "vehiculo":"Moto ficticia","patente":"%s","zona":"Zona ficticia"}
                """.formatted(email, UUID.randomUUID(), patente);
    }
    private Usuario registrar() throws Exception {
        String email = email();
        mvc.perform(post("/auth/register").contentType("application/json").content(cuerpo(email, patente())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("EN_EVALUACION"));
        return usuarios.findByEmail(email).orElseThrow();
    }
    private String admin() {
        Usuario admin = usuarios.save(Usuario.builder().email(email()).password("no-usada")
                .nombre("Prueba").apellido("Admin").DNI(UUID.randomUUID().toString()).telefono("000")
                .rol(Rol.ADMIN).estado(EstadoUsuario.VALIDADO).build());
        return "Bearer " + jwt.generateToken(admin);
    }
    private Repartidor perfil(Usuario usuario) {
        return repartidores.findByUsuarioId(usuario.getId()).orElseThrow();
    }
    private void login(Usuario usuario, int esperado) throws Exception {
        mvc.perform(post("/auth/login").contentType("application/json").content(
                "{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"ClaveFicticia123!\"}"))
                .andExpect(status().is(esperado));
    }
    @Test void registroPendienteConservaDatosSinHabilitarOperaciones() throws Exception {
        Usuario usuario = registrar();
        Repartidor perfil = perfil(usuario);
        assertEquals("Moto ficticia", usuario.getVehiculo());
        assertEquals("Zona ficticia", perfil.getZona());
        assertNotNull(perfil.getPatente());
        assertFalse(perfil.isActivo());
        assertEquals(EstadoRepartidor.NO_DISPONIBLE, perfil.getEstado());
        login(usuario, 401);
        // Incluso un JWT firmado no habilita una cuenta pendiente.
        String token = "Bearer " + jwt.generateToken(usuario);
        mvc.perform(get("/repartidor/me").header("Authorization", token)).andExpect(status().isUnauthorized());
        mvc.perform(get("/repartidor/me/pedidos-disponibles").header("Authorization", token))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/repartidor/me/pedidos/1/tomar").header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }
    @Test void aprobacionAdministrativaHabilitaElMismoPerfilSinPerderDatos() throws Exception {
        Usuario usuario = registrar();
        Repartidor original = perfil(usuario);
        mvc.perform(patch("/usuarios/admin/{id}/validar", usuario.getId()).header("Authorization", admin()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("VALIDADO"));
        Repartidor aprobado = perfil(usuario);
        assertEquals(original.getId(), aprobado.getId());
        assertEquals(original.getPatente(), aprobado.getPatente());
        assertEquals(original.getZona(), aprobado.getZona());
        assertTrue(aprobado.isActivo());
        assertEquals(EstadoRepartidor.DISPONIBLE, aprobado.getEstado());
        Usuario validado = usuarios.findById(usuario.getId()).orElseThrow();
        login(validado, 200);
        mvc.perform(get("/repartidor/me").header("Authorization", "Bearer " + jwt.generateToken(validado)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.vehiculo").value("Moto ficticia"))
                .andExpect(jsonPath("$.patente").value(original.getPatente()));
    }
    @Test void rechazoConservaDatosPeroImpideLoginYActivacionManual() throws Exception {
        Usuario usuario = registrar();
        Repartidor original = perfil(usuario);
        String admin = admin();
        mvc.perform(patch("/usuarios/admin/{id}/rechazar", usuario.getId()).header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RECHAZADO"));
        assertEquals(original.getPatente(), perfil(usuario).getPatente());
        assertEquals(original.getZona(), perfil(usuario).getZona());
        assertFalse(perfil(usuario).isActivo());
        login(usuarios.findById(usuario.getId()).orElseThrow(), 401);
        mvc.perform(patch("/repartidores/{id}/activar", original.getId()).header("Authorization", admin))
                .andExpect(status().isConflict());
        // El contrato actual de State de Usuario responde 400 ante una transición inválida.
        mvc.perform(patch("/usuarios/admin/{id}/validar", usuario.getId()).header("Authorization", admin))
                .andExpect(status().isBadRequest());
    }
    @Test void pendienteNoPuedeActivarseNiRecibirPedidosAdministrativamente() throws Exception {
        Usuario usuario = registrar();
        Repartidor perfil = perfil(usuario);
        Pedido pedido = pedidos.saveAndFlush(Pedido.builder().comercioId(999L).depositoId(999L).fechaCreacion(java.time.LocalDateTime.now()).direccionDestino("Destino ficticio")
                .estado(EstadoPedido.LISTO_PARA_RETIRAR).build());
        String admin = admin();
        mvc.perform(patch("/repartidores/{id}/activar", perfil.getId()).header("Authorization", admin))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/pedidos/{id}/asignar/{repartidor}", pedido.getId(), perfil.getId())
                        .header("Authorization", admin)).andExpect(status().isConflict());
        assertNull(pedidos.findById(pedido.getId()).orElseThrow().getRepartidor());
        assertEquals(EstadoPedido.LISTO_PARA_RETIRAR, pedidos.findById(pedido.getId()).orElseThrow().getEstado());
        assertEquals(0, jdbc.queryForObject("select count(*) from historial_estado_pedido where pedido_id = ?", Integer.class, pedido.getId()));
        assertFalse(perfil(usuario).isActivo());
    }
    @Test void validacionNoEsAccesibleParaOtrosRoles() throws Exception {
        Usuario pendiente = registrar();
        Usuario comercio = usuarios.save(Usuario.builder().email(email()).password("no-usada")
                .nombre("Prueba").apellido("Comercio").DNI(UUID.randomUUID().toString()).telefono("000")
                .rol(Rol.COMERCIO).estado(EstadoUsuario.VALIDADO).build());
        mvc.perform(patch("/usuarios/admin/{id}/validar", pendiente.getId())
                .header("Authorization", "Bearer " + jwt.generateToken(comercio))).andExpect(status().isForbidden());
        assertEquals(EstadoUsuario.EN_EVALUACION, usuarios.findById(pendiente.getId()).orElseThrow().getEstado());
        assertFalse(perfil(pendiente).isActivo());
    }
    @Test void patenteDuplicadaRevierteTambienElUsuarioNuevo() throws Exception {
        Usuario primero = registrar();
        long cantidad = usuarios.count();
        String email = email();
        mvc.perform(post("/auth/register").contentType("application/json")
                .content(cuerpo(email, perfil(primero).getPatente()))).andExpect(status().isConflict());
        assertFalse(usuarios.existsByEmail(email));
        assertEquals(cantidad, usuarios.count());
    }
    @Test void falloDuranteAprobacionRevierteUsuarioYPerfil() throws Exception {
        Usuario usuario = registrar();
        doThrow(new IllegalStateException("Fallo controlado de prueba"))
                .when(notificaciones).notificarActualizacionEstadoUsuario(any());
        mvc.perform(patch("/usuarios/admin/{id}/validar", usuario.getId()).header("Authorization", admin()))
                .andExpect(status().isConflict());
        assertEquals(EstadoUsuario.EN_EVALUACION, usuarios.findById(usuario.getId()).orElseThrow().getEstado());
        assertFalse(perfil(usuario).isActivo());
        assertEquals(EstadoRepartidor.NO_DISPONIBLE, perfil(usuario).getEstado());
    }
}
