package com.example.DA2Back.Seguridad;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.RegistroComercioDTO;
import com.example.DA2Back.Seguridad.dto.RegistroDepositoDTO;
import com.example.DA2Back.Seguridad.dto.RegistroRepartidorDTO;
import com.example.DA2Back.Seguridad.negocio.UsuarioFactory;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.repartidor.dto.RepartidorCreateDTO;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioFactoryTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IComercio comercioService;
    @Mock private IRepartidorService repartidorService;

    private UsuarioFactory factory;

    @BeforeEach
    void setUp() {
        factory = new UsuarioFactory(passwordEncoder, comercioService, repartidorService);
        when(passwordEncoder.encode("ClaveSegura1")).thenReturn("HASH-1");
    }

    @Test
    @DisplayName("Registro de comercio: usuario EN_EVALUACION con la clave hasheada (nunca en claro)")
    void crearDesdeRegistro_comercio() {
        Usuario u = factory.crearDesdeRegistro(base(mock(RegistroComercioDTO.class), Rol.COMERCIO));

        assertEquals(Rol.COMERCIO, u.getRol());
        assertEquals(EstadoUsuario.EN_EVALUACION, u.getEstado());
        assertEquals("HASH-1", u.getPassword());
        assertEquals("ana@mail.com", u.getEmail());
        assertEquals("Ana", u.getNombre());
        assertEquals("Paz", u.getApellido());
        assertEquals("30111222", u.getDNI());
        assertEquals("1155556666", u.getTelefono());
        assertNull(u.getVehiculo());
    }

    @Test
    @DisplayName("Registro de repartidor conserva el vehiculo en el usuario")
    void crearDesdeRegistro_repartidor() {
        RegistroRepartidorDTO dto = new RegistroRepartidorDTO();
        base(dto, Rol.REPARTIDOR);
        dto.setVehiculo("Moto 150cc");

        Usuario u = factory.crearDesdeRegistro(dto);

        assertEquals(Rol.REPARTIDOR, u.getRol());
        assertEquals("Moto 150cc", u.getVehiculo());
        assertEquals(EstadoUsuario.EN_EVALUACION, u.getEstado());
    }

    @Test
    @DisplayName("No se puede auto-registrar un ADMIN ni un DEPOSITO")
    void crearDesdeRegistro_rolesProhibidos() {
        IllegalArgumentException admin = assertThrows(IllegalArgumentException.class,
                () -> factory.crearDesdeRegistro(base(mock(RegistroComercioDTO.class), Rol.ADMIN)));
        assertEquals("El rol ADMIN no puede crearse por auto-registro", admin.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> factory.crearDesdeRegistro(base(mock(RegistroComercioDTO.class), Rol.DEPOSITO)));
    }

    @Test
    @DisplayName("Usuario de deposito: rol DEPOSITO, EN_EVALUACION y clave hasheada")
    void crearUsuarioDeposito() {
        RegistroDepositoDTO dto = mock(RegistroDepositoDTO.class);
        base(dto, null);

        Usuario u = factory.crearUsuarioDeposito(dto);

        assertEquals(Rol.DEPOSITO, u.getRol());
        assertEquals(EstadoUsuario.EN_EVALUACION, u.getEstado());
        assertEquals("HASH-1", u.getPassword());
    }

    @Test
    @DisplayName("Administrador: queda VALIDADO con rol ADMIN y la clave hasheada")
    void crearAdministrador() {
        when(passwordEncoder.encode("admin123")).thenReturn("HASH-ADMIN");

        Usuario u = factory.crearAdministrador("admin@x.com", "admin123", "Admin", "Root", "1", "2");

        assertEquals(Rol.ADMIN, u.getRol());
        assertEquals(EstadoUsuario.VALIDADO, u.getEstado());
        assertEquals("HASH-ADMIN", u.getPassword());
        assertEquals("admin@x.com", u.getEmail());
        assertEquals("1", u.getDNI());
        assertEquals("2", u.getTelefono());
    }

    @Test
    @DisplayName("Entidad relacionada de un comercio: crea el comercio con los datos del registro")
    void crearEntidadRelacionada_comercio() {
        RegistroComercioDTO dto = mock(RegistroComercioDTO.class);
        base(dto, Rol.COMERCIO);
        when(dto.getNombreComercial()).thenReturn("Urban Shoes");
        when(dto.getRazonSocial()).thenReturn("Urban SA");
        when(dto.getDireccion()).thenReturn("Calle 1");
        when(dto.getCuit()).thenReturn("20-123");

        factory.crearEntidadRelacionada(dto, 7L);

        ArgumentCaptor<ComercioCreateDTO> captor = ArgumentCaptor.forClass(ComercioCreateDTO.class);
        verify(comercioService).crear(captor.capture(), org.mockito.ArgumentMatchers.eq(7L));
        assertEquals("Urban Shoes", captor.getValue().getNombreComercial());
        assertEquals("Urban SA", captor.getValue().getRazonSocial());
        assertEquals("Calle 1", captor.getValue().getDireccion());
        assertEquals("20-123", captor.getValue().getCuit());
        assertEquals("1155556666", captor.getValue().getTelefono());
        assertEquals("ana@mail.com", captor.getValue().getEmail());
        verify(repartidorService, never()).crearPendiente(any());
    }

    @Test
    @DisplayName("Entidad relacionada de un repartidor: crea el perfil pendiente con usuario, patente y zona")
    void crearEntidadRelacionada_repartidor() {
        RegistroRepartidorDTO dto = new RegistroRepartidorDTO();
        base(dto, Rol.REPARTIDOR);
        dto.setPatente("AB123CD");
        dto.setZona("Palermo");

        factory.crearEntidadRelacionada(dto, 8L);

        ArgumentCaptor<RepartidorCreateDTO> captor = ArgumentCaptor.forClass(RepartidorCreateDTO.class);
        verify(repartidorService).crearPendiente(captor.capture());
        assertEquals(8L, captor.getValue().getUsuarioId());
        assertEquals("AB123CD", captor.getValue().getPatente());
        assertEquals("Palermo", captor.getValue().getZona());
        verify(comercioService, never()).crear(any(), any());
    }

    @Test
    @DisplayName("Otros tipos de registro (deposito) no crean entidades relacionadas")
    void crearEntidadRelacionada_otros() {
        factory.crearEntidadRelacionada(mock(RegistroDepositoDTO.class), 9L);

        verify(comercioService, never()).crear(any(), any());
        verify(repartidorService, never()).crearPendiente(any());
    }

    // ───────────── helpers ─────────────

    /** Completa los datos comunes; funciona tanto con DTOs reales (setters) como con mocks (getters). */
    private RegisterDTO base(RegisterDTO dto, Rol rol) {
        if (org.mockito.Mockito.mockingDetails(dto).isMock()) {
            when(dto.getEmail()).thenReturn("ana@mail.com");
            when(dto.getPassword()).thenReturn("ClaveSegura1");
            when(dto.getNombre()).thenReturn("Ana");
            when(dto.getApellido()).thenReturn("Paz");
            when(dto.getDni()).thenReturn("30111222");
            when(dto.getTelefono()).thenReturn("1155556666");
            when(dto.getRol()).thenReturn(rol);
        } else {
            dto.setEmail("ana@mail.com");
            dto.setPassword("ClaveSegura1");
            dto.setNombre("Ana");
            dto.setApellido("Paz");
            dto.setDni("30111222");
            dto.setTelefono("1155556666");
            dto.setRol(rol);
        }
        return dto;
    }
}
