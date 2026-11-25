package com.example.DA2Back.Seguridad.negocio;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.negocio.State.EstadoBloqueado;
import com.example.DA2Back.Seguridad.negocio.State.EstadoEnEvaluacion;
import com.example.DA2Back.Seguridad.negocio.State.EstadoRechazado;
import com.example.DA2Back.Seguridad.negocio.State.EstadoValidado;
import com.example.DA2Back.Seguridad.negocio.State.ResolverEstadoUsuario;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Seed del administrador (A) y bloqueo por intentos fallidos de login (SEC-W01). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminSeederYControlIntentosTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioFactory usuarioFactory;
    @Mock private NotificacionService notificacionService;

    private AdminSeeder seeder;
    private ControlIntentosLoginService control;

    @BeforeEach
    void setUp() {
        seeder = new AdminSeeder(usuarioRepository, usuarioFactory);
        ReflectionTestUtils.setField(seeder, "adminEmail", "root@logired.com");
        ReflectionTestUtils.setField(seeder, "adminPassword", "pw-seed");
        ReflectionTestUtils.setField(seeder, "adminNombre", "Root");
        ReflectionTestUtils.setField(seeder, "adminApellido", "Admin");
        ReflectionTestUtils.setField(seeder, "adminDni", "11");
        ReflectionTestUtils.setField(seeder, "adminTelefono", "22");

        ResolverEstadoUsuario estados = new ResolverEstadoUsuario(
                new EstadoEnEvaluacion(), new EstadoValidado(), new EstadoBloqueado(), new EstadoRechazado());
        control = new ControlIntentosLoginService(usuarioRepository, estados, notificacionService);
    }

    // ───────────── AdminSeeder ─────────────

    @Test
    @DisplayName("Seed: si ya existe un ADMIN no crea nada")
    void seeder_yaHayAdmin() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario(1L, Rol.ADMIN, EstadoUsuario.VALIDADO, "a@x.com")));

        seeder.run();

        verify(usuarioFactory, never()).crearAdministrador(any(), any(), any(), any(), any(), any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Seed: si el email configurado ya lo usa otro usuario no crea el admin")
    void seeder_emailOcupado() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO, "c@x.com")));
        when(usuarioRepository.existsByEmail("root@logired.com")).thenReturn(true);

        seeder.run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Seed: sin admin ni conflicto de email crea y guarda el administrador con los datos configurados")
    void seeder_creaAdmin() {
        when(usuarioRepository.findAll()).thenReturn(List.of());
        when(usuarioRepository.existsByEmail("root@logired.com")).thenReturn(false);
        Usuario admin = usuario(9L, Rol.ADMIN, EstadoUsuario.VALIDADO, "root@logired.com");
        when(usuarioFactory.crearAdministrador("root@logired.com", "pw-seed", "Root", "Admin", "11", "22"))
                .thenReturn(admin);

        seeder.run();

        verify(usuarioRepository).save(admin);
    }

    // ───────────── ControlIntentosLoginService ─────────────

    @Test
    @DisplayName("Los primeros 4 intentos fallidos no bloquean; el 5.o bloquea la cuenta, guarda y notifica")
    void quintoIntentoBloquea() {
        Usuario u = usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO, "ana@mail.com");
        when(usuarioRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(u));

        for (int i = 1; i <= 4; i++) {
            assertFalse(control.registrarIntentoFallido("ana@mail.com"), "intento " + i);
            assertEquals(i, control.obtenerIntentosFallidos("ana@mail.com"));
        }
        assertEquals(EstadoUsuario.VALIDADO, u.getEstado());

        assertTrue(control.registrarIntentoFallido("ana@mail.com"), "el 5.o intento bloquea");

        assertEquals(EstadoUsuario.BLOQUEADO, u.getEstado());
        verify(usuarioRepository).save(u);
        verify(notificacionService).notificarActualizacionEstadoUsuario(u);
        assertEquals(0, control.obtenerIntentosFallidos("ana@mail.com"), "el contador se reinicia tras bloquear");
    }

    @Test
    @DisplayName("El umbral es configurable (3)")
    void umbralConfigurable() {
        ReflectionTestUtils.setField(control, "maxIntentosFallidos", 3);
        when(usuarioRepository.findByEmail("ana@mail.com"))
                .thenReturn(Optional.of(usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO, "ana@mail.com")));

        assertFalse(control.registrarIntentoFallido("ana@mail.com"));
        assertFalse(control.registrarIntentoFallido("ana@mail.com"));
        assertTrue(control.registrarIntentoFallido("ana@mail.com"));
    }

    @Test
    @DisplayName("El email se normaliza (espacios y mayusculas) y null cuenta como cadena vacia")
    void normalizaEmail() {
        control.registrarIntentoFallido("  ANA@Mail.com ");
        control.registrarIntentoFallido("ana@mail.com");

        assertEquals(2, control.obtenerIntentosFallidos("ana@mail.com"));
        assertEquals(0, control.obtenerIntentosFallidos(null));
        control.registrarIntentoFallido(null);
        assertEquals(1, control.obtenerIntentosFallidos(null));
    }

    @Test
    @DisplayName("Un login exitoso reinicia el contador solo de ese email")
    void loginExitosoReinicia() {
        control.registrarIntentoFallido("ana@mail.com");
        control.registrarIntentoFallido("ana@mail.com");
        control.registrarIntentoFallido("otro@mail.com");

        control.registrarLoginExitoso(" ANA@mail.com");

        assertEquals(0, control.obtenerIntentosFallidos("ana@mail.com"));
        assertEquals(1, control.obtenerIntentosFallidos("otro@mail.com"));
    }

    @Test
    @DisplayName("Cuenta que no admite bloqueo (EN_EVALUACION) o inexistente: no falla, no guarda y no notifica")
    void bloqueoNoAplicable() {
        ReflectionTestUtils.setField(control, "maxIntentosFallidos", 1);
        when(usuarioRepository.findByEmail("eval@mail.com"))
                .thenReturn(Optional.of(usuario(1L, Rol.COMERCIO, EstadoUsuario.EN_EVALUACION, "eval@mail.com")));
        when(usuarioRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());

        assertTrue(control.registrarIntentoFallido("eval@mail.com"));
        assertTrue(control.registrarIntentoFallido("nadie@mail.com"));

        verify(usuarioRepository, never()).save(any());
        verify(notificacionService, never()).notificarActualizacionEstadoUsuario(any());
    }

    private Usuario usuario(Long id, Rol rol, EstadoUsuario estado, String email) {
        return Usuario.builder().id(id).rol(rol).estado(estado).email(email).build();
    }
}
