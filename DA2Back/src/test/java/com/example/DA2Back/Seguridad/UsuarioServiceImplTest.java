package com.example.DA2Back.Seguridad;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.dto.ActualizarUsuarioAdminDTO;
import com.example.DA2Back.Seguridad.dto.CambiarPasswordAdminDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.RegistroDepositoDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.UsuarioAdminDetalleDTO;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;
import com.example.DA2Back.Seguridad.excepcion.UsuarioYaExisteException;
import com.example.DA2Back.Seguridad.negocio.State.EstadoBloqueado;
import com.example.DA2Back.Seguridad.negocio.State.EstadoEnEvaluacion;
import com.example.DA2Back.Seguridad.negocio.State.EstadoRechazado;
import com.example.DA2Back.Seguridad.negocio.State.EstadoValidado;
import com.example.DA2Back.Seguridad.negocio.State.ResolverEstadoUsuario;
import com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin.IPerfilAdmin;
import com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin.ResolverPerfilAdmin;
import com.example.DA2Back.Seguridad.negocio.UsuarioFactory;
import com.example.DA2Back.Seguridad.negocio.UsuarioServiceImpl;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dto.AsociarDepositoDTO;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.excepcion.AsociacionInvalidaException;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioFactory usuarioFactory;
    @Mock private ResolverPerfilAdmin resolverPerfilAdmin;
    @Mock private IDeposito depositoService;
    @Mock private IComercio comercioService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificacionService notificacionService;
    @Mock private IRepartidorService repartidorService;

    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        ResolverEstadoUsuario estados = new ResolverEstadoUsuario(
                new EstadoEnEvaluacion(), new EstadoValidado(), new EstadoBloqueado(), new EstadoRechazado());
        service = new UsuarioServiceImpl(usuarioRepository, usuarioFactory, estados, resolverPerfilAdmin,
                depositoService, comercioService, passwordEncoder, notificacionService, repartidorService);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("HASH");
    }

    // ───────────── registro ─────────────

    @Test
    @DisplayName("Registrar con un email ya usado -> UsuarioYaExiste y no se persiste nada")
    void registrar_emailDuplicado() {
        RegisterDTO dto = mock(RegisterDTO.class);
        when(dto.getEmail()).thenReturn("dup@mail.com");
        when(usuarioRepository.existsByEmail("dup@mail.com")).thenReturn(true);

        assertThrows(UsuarioYaExisteException.class, () -> service.registrar(dto));

        verify(usuarioRepository, never()).save(any());
        verify(notificacionService, never()).notificarSolicitudRegistro(any());
    }

    @Test
    @DisplayName("Registrar guarda el usuario, crea su entidad relacionada y notifica la solicitud")
    void registrar_ok() {
        RegisterDTO dto = mock(RegisterDTO.class);
        when(dto.getEmail()).thenReturn("nuevo@mail.com");
        when(usuarioRepository.existsByEmail("nuevo@mail.com")).thenReturn(false);
        Usuario creado = usuario(5L, Rol.COMERCIO, EstadoUsuario.EN_EVALUACION);
        when(usuarioFactory.crearDesdeRegistro(dto)).thenReturn(creado);

        UsuarioResponseDTO res = service.registrar(dto);

        assertEquals(5L, res.getId());
        assertEquals(EstadoUsuario.EN_EVALUACION, res.getEstado());
        verify(usuarioFactory).crearEntidadRelacionada(dto, 5L);
        verify(notificacionService).notificarSolicitudRegistro(creado);
    }

    // ───────────── deposito de un comercio ─────────────

    @Test
    @DisplayName("Registrar deposito: crea el usuario deposito y el deposito asociado al comercio autenticado")
    void registrarDeposito_ok() {
        RegistroDepositoDTO dto = depositoDto("dep@mail.com", "30111222");
        when(comercioService.obtenerPorUsuarioId(10L)).thenReturn(ComercioResponseDTO.builder().id(1L).build());
        Usuario usuarioDeposito = usuario(40L, Rol.DEPOSITO, EstadoUsuario.VALIDADO);
        when(usuarioFactory.crearUsuarioDeposito(dto)).thenReturn(usuarioDeposito);
        DepositoResponseDTO esperado = DepositoResponseDTO.builder().id(9L).build();
        when(depositoService.crear(any(DepositoCreateDTO.class), anyLong(), anyLong())).thenReturn(esperado);

        DepositoResponseDTO res = service.registrarDepositoParaComercio(dto, 10L);

        assertSame(esperado, res);
        ArgumentCaptor<DepositoCreateDTO> captor = ArgumentCaptor.forClass(DepositoCreateDTO.class);
        verify(depositoService).crear(captor.capture(), org.mockito.ArgumentMatchers.eq(40L),
                org.mockito.ArgumentMatchers.eq(1L));
        assertEquals("Deposito Norte", captor.getValue().getNombre());
        assertEquals("Av. Siempre Viva 1", captor.getValue().getDireccion());
    }

    @Test
    @DisplayName("Registrar deposito: cupo lleno, email o DNI duplicados fallan antes de crear usuario")
    void registrarDeposito_rechazos() {
        RegistroDepositoDTO dto = depositoDto("dep@mail.com", "30111222");
        when(comercioService.obtenerPorUsuarioId(10L)).thenReturn(ComercioResponseDTO.builder().id(1L).build());

        doThrow(new AsociacionInvalidaException("cupo")).when(depositoService).validarCupoDisponible(1L);
        assertThrows(AsociacionInvalidaException.class, () -> service.registrarDepositoParaComercio(dto, 10L));
        verify(usuarioRepository, never()).existsByEmail(any());

        org.mockito.Mockito.reset(depositoService);
        when(usuarioRepository.existsByEmail("dep@mail.com")).thenReturn(true);
        assertEquals("Ya existe un usuario con ese email",
                assertThrows(UsuarioYaExisteException.class,
                        () -> service.registrarDepositoParaComercio(dto, 10L)).getMessage());

        when(usuarioRepository.existsByEmail("dep@mail.com")).thenReturn(false);
        when(usuarioRepository.existsPorDni("30111222")).thenReturn(true);
        assertEquals("Ya existe un usuario con ese DNI",
                assertThrows(UsuarioYaExisteException.class,
                        () -> service.registrarDepositoParaComercio(dto, 10L)).getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    // ───────────── consultas ─────────────

    @Test
    @DisplayName("Consultas: listar, por id y por email (encontrado y no encontrado)")
    void consultas() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario(1L, Rol.ADMIN, EstadoUsuario.VALIDADO)));
        assertEquals(1, service.listarTodos().size());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, Rol.ADMIN, EstadoUsuario.VALIDADO)));
        assertEquals("u1@mail.com", service.obtenerPorId(1L).getEmail());
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals("Usuario no encontrado: 9",
                assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(9L)).getMessage());

        when(usuarioRepository.findByEmail("u1@mail.com"))
                .thenReturn(Optional.of(usuario(1L, Rol.ADMIN, EstadoUsuario.VALIDADO)));
        assertEquals(1L, service.obtenerPorEmail("u1@mail.com").getId());
        when(usuarioRepository.findByEmail("x@mail.com")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorEmail("x@mail.com"));
    }

    @Test
    @DisplayName("existePorId delega en el repositorio")
    void existePorId() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        assertTrue(service.existePorId(1L));
        assertFalse(service.existePorId(2L));
    }

    // ───────────── actualizacion de datos por admin ─────────────

    @Test
    @DisplayName("Actualizar usuario: inexistente, y email nuevo ya usado -> errores sin modificar")
    void actualizarUsuario_errores() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizarUsuarioAdmin(9L, adminDto("a@mail.com")));

        Usuario u = usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.existsByEmail("otro@mail.com")).thenReturn(true);

        assertThrows(UsuarioYaExisteException.class, () -> service.actualizarUsuarioAdmin(1L, adminDto("otro@mail.com")));

        assertEquals("u1@mail.com", u.getEmail());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar usuario con el MISMO email (ignorando mayusculas) no revalida unicidad y guarda los campos")
    void actualizarUsuario_mismoEmail() {
        Usuario u = usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        UsuarioResponseDTO res = service.actualizarUsuarioAdmin(1L, adminDto("U1@MAIL.COM"));

        verify(usuarioRepository, never()).existsByEmail(any());
        assertEquals("Nuevo", res.getNombre());
        assertEquals("Apellido Nuevo", res.getApellido());
        assertEquals("1122334455", res.getTelefono());
        assertEquals("U1@MAIL.COM", res.getEmail());
        assertEquals("99887766", res.getDni());
    }

    @Test
    @DisplayName("Actualizar usuario con email nuevo y libre lo cambia")
    void actualizarUsuario_emailNuevoLibre() {
        Usuario u = usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.existsByEmail("libre@mail.com")).thenReturn(false);

        assertEquals("libre@mail.com", service.actualizarUsuarioAdmin(1L, adminDto("libre@mail.com")).getEmail());
    }

    @Test
    @DisplayName("Resetear password guarda el HASH (nunca el texto plano); usuario inexistente falla")
    void resetearPassword() {
        Usuario u = usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        CambiarPasswordAdminDTO dto = new CambiarPasswordAdminDTO();
        dto.setPasswordNuevo("ClaveNueva123");

        service.resetearPasswordAdmin(1L, dto);

        assertEquals("HASH", u.getPassword());
        verify(passwordEncoder).encode("ClaveNueva123");
        verify(usuarioRepository).save(u);

        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.resetearPasswordAdmin(9L, dto));
    }

    // ───────────── transiciones de estado ─────────────

    @Test
    @DisplayName("Validar un usuario EN_EVALUACION: queda VALIDADO, se guarda y se notifica")
    void validar_ok() {
        Usuario u = bloquearParaActualizar(usuario(1L, Rol.COMERCIO, EstadoUsuario.EN_EVALUACION));

        UsuarioResponseDTO res = service.validar(1L);

        assertEquals(EstadoUsuario.VALIDADO, res.getEstado());
        verify(notificacionService).notificarActualizacionEstadoUsuario(u);
        verify(repartidorService, never()).resolverEvaluacion(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Rechazar / bloquear / desbloquear aplican la transicion correspondiente")
    void otrasTransiciones() {
        bloquearParaActualizar(usuario(1L, Rol.COMERCIO, EstadoUsuario.EN_EVALUACION));
        assertEquals(EstadoUsuario.RECHAZADO, service.rechazar(1L).getEstado());

        bloquearParaActualizar(usuario(2L, Rol.COMERCIO, EstadoUsuario.VALIDADO));
        assertEquals(EstadoUsuario.BLOQUEADO, service.bloquear(2L).getEstado());

        bloquearParaActualizar(usuario(3L, Rol.COMERCIO, EstadoUsuario.BLOQUEADO));
        assertEquals(EstadoUsuario.VALIDADO, service.desbloquear(3L).getEstado());
    }

    @Test
    @DisplayName("Validar o rechazar un REPARTIDOR en evaluacion resuelve su evaluacion (true/false)")
    void repartidorEnEvaluacion_resuelveEvaluacion() {
        bloquearParaActualizar(usuario(1L, Rol.REPARTIDOR, EstadoUsuario.EN_EVALUACION));
        service.validar(1L);
        verify(repartidorService).resolverEvaluacion(1L, true);

        bloquearParaActualizar(usuario(2L, Rol.REPARTIDOR, EstadoUsuario.EN_EVALUACION));
        service.rechazar(2L);
        verify(repartidorService).resolverEvaluacion(2L, false);
    }

    @Test
    @DisplayName("Bloquear un REPARTIDOR ya validado NO resuelve evaluacion")
    void repartidorValidado_noResuelveEvaluacion() {
        bloquearParaActualizar(usuario(1L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO));

        service.bloquear(1L);

        verify(repartidorService, never()).resolverEvaluacion(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Transicion invalida: no se guarda ni se notifica y el estado no cambia")
    void transicionInvalida() {
        Usuario u = bloquearParaActualizar(usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO));

        assertThrows(TransicionInvalidaException.class, () -> service.validar(1L));

        assertEquals(EstadoUsuario.VALIDADO, u.getEstado());
        verify(usuarioRepository, never()).save(any());
        verify(notificacionService, never()).notificarActualizacionEstadoUsuario(any());
    }

    @Test
    @DisplayName("Transicion sobre un usuario inexistente -> RecursoNoEncontrado")
    void transicion_inexistente() {
        when(usuarioRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.validar(9L));
    }

    @Test
    @DisplayName("Asociar deposito a comercio delega con ambos ids")
    void asociarDeposito() {
        AsociarDepositoDTO dto = new AsociarDepositoDTO();
        dto.setDepositoId(3L);
        dto.setComercioId(1L);

        service.asociarDepositoAComercio(dto);

        verify(depositoService).asociarAComercio(3L, 1L);
    }

    // ───────────── perfil de administracion (Strategy) ─────────────

    @Test
    @DisplayName("Detalle admin incluye el perfil de su rol; si el rol no tiene perfil, viene null")
    void detalleAdmin() {
        Usuario u = usuario(1L, Rol.COMERCIO, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        IPerfilAdmin estrategia = mock(IPerfilAdmin.class);
        PerfilAdminDTO perfil = mock(PerfilAdminDTO.class);
        when(estrategia.obtener(u)).thenReturn(Optional.of(perfil));
        when(resolverPerfilAdmin.resolver(Rol.COMERCIO)).thenReturn(Optional.of(estrategia));

        UsuarioAdminDetalleDTO con = service.obtenerDetalleAdmin(1L);

        assertSame(perfil, con.getPerfil());
        assertEquals(1L, con.getUsuario().getId());

        Usuario admin = usuario(2L, Rol.ADMIN, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(resolverPerfilAdmin.resolver(Rol.ADMIN)).thenReturn(Optional.empty());
        assertNull(service.obtenerDetalleAdmin(2L).getPerfil());

        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerDetalleAdmin(9L));
    }

    @Test
    @DisplayName("Actualizar perfil admin delega en la estrategia del rol y guarda al usuario")
    void actualizarPerfilAdmin_ok() {
        Usuario u = usuario(1L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        IPerfilAdmin estrategia = mock(IPerfilAdmin.class);
        when(estrategia.obtener(u)).thenReturn(Optional.empty());
        when(resolverPerfilAdmin.resolver(Rol.REPARTIDOR)).thenReturn(Optional.of(estrategia));
        ActualizarPerfilAdminDTO dto = mock(ActualizarPerfilAdminDTO.class);

        UsuarioAdminDetalleDTO res = service.actualizarPerfilAdmin(1L, dto);

        verify(estrategia).actualizar(u, dto);
        verify(usuarioRepository).save(u);
        assertEquals(1L, res.getUsuario().getId());
    }

    @Test
    @DisplayName("Actualizar perfil de un rol sin perfil editable -> IllegalArgument sin guardar")
    void actualizarPerfilAdmin_sinPerfilEditable() {
        Usuario admin = usuario(2L, Rol.ADMIN, EstadoUsuario.VALIDADO);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(resolverPerfilAdmin.resolver(Rol.ADMIN)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.actualizarPerfilAdmin(2L, mock(ActualizarPerfilAdminDTO.class)));

        assertTrue(ex.getMessage().contains("ADMIN"));
        verify(usuarioRepository, never()).save(any());
    }

    // ───────────── helpers ─────────────

    private Usuario bloquearParaActualizar(Usuario u) {
        when(usuarioRepository.findByIdForUpdate(u.getId())).thenReturn(Optional.of(u));
        return u;
    }

    private Usuario usuario(Long id, Rol rol, EstadoUsuario estado) {
        return Usuario.builder().id(id).rol(rol).estado(estado).nombre("Nombre" + id).apellido("Apellido")
                .email("u" + id + "@mail.com").telefono("1100000000").DNI("3000000" + id).password("plain").build();
    }

    private ActualizarUsuarioAdminDTO adminDto(String email) {
        ActualizarUsuarioAdminDTO dto = new ActualizarUsuarioAdminDTO();
        dto.setNombre("Nuevo");
        dto.setApellido("Apellido Nuevo");
        dto.setTelefono("1122334455");
        dto.setEmail(email);
        dto.setDni("99887766");
        return dto;
    }

    private RegistroDepositoDTO depositoDto(String email, String dni) {
        RegistroDepositoDTO dto = mock(RegistroDepositoDTO.class);
        when(dto.getEmail()).thenReturn(email);
        when(dto.getDni()).thenReturn(dni);
        when(dto.getNombreDeposito()).thenReturn("Deposito Norte");
        when(dto.getDireccionDeposito()).thenReturn("Av. Siempre Viva 1");
        return dto;
    }
}
