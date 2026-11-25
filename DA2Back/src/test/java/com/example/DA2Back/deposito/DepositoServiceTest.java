package com.example.DA2Back.deposito;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.excepcion.AsociacionInvalidaException;
import com.example.DA2Back.deposito.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.deposito.negocio.DepositoService;
import com.example.DA2Back.deposito.negocio.IDeposito;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepositoServiceTest {

    @Mock private DepositoRepository depositoRepository;
    @Mock private ComercioRepository comercioRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DepositoService service;

    // ───────────── consultas ─────────────

    @Test
    @DisplayName("obtenerPorId: id nulo -> IllegalArgument; inexistente -> RecursoNoEncontrado")
    void obtenerPorId_invalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(null));
        when(depositoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(9L));
    }

    @Test
    @DisplayName("obtenerPorId arma el DTO con los datos del responsable")
    void obtenerPorId_ok_conResponsable() {
        Deposito d = deposito(1L, "Central", 5L);
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(d));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario(5L, "Ana", "Paz")));

        DepositoResponseDTO dto = service.obtenerPorId(1L);

        assertEquals("Central", dto.getNombre());
        assertEquals("Ana Paz", dto.getResponsable());
        assertEquals(EstadoUsuario.VALIDADO, dto.getUsuarioEstado());
    }

    @Test
    @DisplayName("obtenerPorId sin usuario responsable ni comercio deja esos campos en null")
    void obtenerPorId_sinResponsable() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito(1L, "Central", 5L)));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.empty());

        DepositoResponseDTO dto = service.obtenerPorId(1L);

        assertNull(dto.getResponsable());
        assertNull(dto.getComercioId());
    }

    @Test
    @DisplayName("obtenerPorUsuarioId: nulo, sin deposito y ok")
    void obtenerPorUsuarioId() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorUsuarioId(null));
        when(depositoRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorUsuarioId(5L));

        when(depositoRepository.findByUsuarioId(6L)).thenReturn(Optional.of(deposito(2L, "Sur", 6L)));
        assertEquals("Sur", service.obtenerPorUsuarioId(6L).getNombre());
    }

    @Test
    @DisplayName("obtenerTodos y obtenerPorComercio resuelven responsables en lote")
    void listados() {
        Deposito d = deposito(1L, "Central", 5L);
        when(depositoRepository.findAll()).thenReturn(List.of(d));
        when(depositoRepository.findByComercioId(3L)).thenReturn(List.of(d));
        when(usuarioRepository.findAllById(anyCollection())).thenReturn(List.of(usuario(5L, "Ana", "Paz")));

        assertEquals("Ana Paz", service.obtenerTodos().get(0).getResponsable());
        assertEquals(1, service.obtenerPorComercio(3L).size());
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorComercio(null));
    }

    @Test
    @DisplayName("obtenerEntidadPorId: nulo, inexistente y ok")
    void obtenerEntidadPorId() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerEntidadPorId(null));
        when(depositoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerEntidadPorId(9L));
        Deposito d = deposito(1L, "Central", 5L);
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(d));
        assertSame(d, service.obtenerEntidadPorId(1L));
    }

    // ───────────── cupo ─────────────

    @Test
    @DisplayName("validarCupoDisponible: nulo, cupo lleno (10) y con lugar")
    void validarCupo() {
        assertThrows(IllegalArgumentException.class, () -> service.validarCupoDisponible(null));

        when(depositoRepository.countByComercioId(1L)).thenReturn((long) IDeposito.MAX_DEPOSITOS_POR_COMERCIO);
        assertThrows(AsociacionInvalidaException.class, () -> service.validarCupoDisponible(1L));

        when(depositoRepository.countByComercioId(2L)).thenReturn(IDeposito.MAX_DEPOSITOS_POR_COMERCIO - 1L);
        service.validarCupoDisponible(2L); // no lanza: borde inferior del limite
    }

    // ───────────── crear ─────────────

    @Test
    @DisplayName("crear(dto, usuario): valida nulos y persiste")
    void crear_simple() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(null, 5L));
        assertThrows(IllegalArgumentException.class, () -> service.crear(new DepositoCreateDTO("N", "D"), null));

        when(depositoRepository.save(any(Deposito.class))).thenAnswer(i -> {
            Deposito d = i.getArgument(0);
            d.setId(10L);
            return d;
        });
        DepositoResponseDTO res = service.crear(new DepositoCreateDTO("Norte", "Av. 1"), 5L);

        assertEquals(10L, res.getId());
        assertEquals("Norte", res.getNombre());
        assertEquals(5L, res.getUsuarioId());
    }

    @Test
    @DisplayName("crear(dto, usuario, comercio): valida nulos, comercio inexistente y cupo; asocia el comercio")
    void crear_conComercio() {
        DepositoCreateDTO dto = new DepositoCreateDTO("Norte", "Av. 1");
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto, 5L, null));
        assertThrows(IllegalArgumentException.class, () -> service.crear(null, 5L, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto, null, 1L));

        when(comercioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(dto, 5L, 404L));

        Comercio comercio = Comercio.builder().id(1L).nombreComercial("Urban").build();
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercio));
        when(depositoRepository.countByComercioId(1L)).thenReturn(10L);
        assertThrows(AsociacionInvalidaException.class, () -> service.crear(dto, 5L, 1L));
        verify(depositoRepository, never()).save(any());

        when(depositoRepository.countByComercioId(1L)).thenReturn(0L);
        when(depositoRepository.save(any(Deposito.class))).thenAnswer(i -> i.getArgument(0));
        DepositoResponseDTO res = service.crear(dto, 5L, 1L);
        assertEquals(1L, res.getComercioId());
        assertEquals("Urban", res.getComercioNombre());
    }

    // ───────────── actualizar / asociar / eliminar ─────────────

    @Test
    @DisplayName("actualizar: valida entradas, inexistente y modifica nombre y direccion")
    void actualizar() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(null, new DepositoCreateDTO()));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, null));
        when(depositoRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(404L, new DepositoCreateDTO("a", "b")));

        Deposito d = deposito(1L, "Viejo", 5L);
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(d));
        when(depositoRepository.save(d)).thenReturn(d);
        DepositoResponseDTO res = service.actualizar(1L, new DepositoCreateDTO("Nuevo", "Calle 9"));
        assertEquals("Nuevo", res.getNombre());
        assertEquals("Calle 9", res.getDireccion());
    }

    @Test
    @DisplayName("asociarAComercio: nulos, inexistentes, ya asociado y cupo lleno")
    void asociarAComercio_errores() {
        assertThrows(IllegalArgumentException.class, () -> service.asociarAComercio(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.asociarAComercio(1L, null));

        when(depositoRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.asociarAComercio(404L, 1L));

        Deposito yaAsociado = deposito(2L, "X", 5L);
        yaAsociado.setComercio(Comercio.builder().id(7L).build());
        when(depositoRepository.findById(2L)).thenReturn(Optional.of(yaAsociado));
        assertThrows(AsociacionInvalidaException.class, () -> service.asociarAComercio(2L, 1L));

        Deposito libre = deposito(3L, "Y", 5L);
        when(depositoRepository.findById(3L)).thenReturn(Optional.of(libre));
        when(comercioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.asociarAComercio(3L, 404L));

        when(comercioRepository.findById(1L)).thenReturn(Optional.of(Comercio.builder().id(1L).build()));
        when(depositoRepository.countByComercioId(1L)).thenReturn(10L);
        assertThrows(AsociacionInvalidaException.class, () -> service.asociarAComercio(3L, 1L));
        verify(depositoRepository, never()).save(any());
    }

    @Test
    @DisplayName("asociarAComercio exitoso guarda el deposito con su comercio")
    void asociarAComercio_ok() {
        Deposito libre = deposito(3L, "Y", 5L);
        Comercio comercio = Comercio.builder().id(1L).build();
        when(depositoRepository.findById(3L)).thenReturn(Optional.of(libre));
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercio));
        when(depositoRepository.countByComercioId(1L)).thenReturn(3L);

        service.asociarAComercio(3L, 1L);

        assertSame(comercio, libre.getComercio());
        verify(depositoRepository).save(libre);
    }

    @Test
    @DisplayName("eliminar: nulo, inexistente y ok")
    void eliminar() {
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(null));
        when(depositoRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(404L));

        Deposito d = deposito(1L, "Central", 5L);
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(d));
        service.eliminar(1L);
        verify(depositoRepository).delete(d);
    }

    // ───────────── helpers ─────────────

    private Deposito deposito(Long id, String nombre, Long usuarioId) {
        Deposito d = new Deposito();
        d.setId(id);
        d.setNombre(nombre);
        d.setDireccion("Direccion " + nombre);
        d.setUsuarioId(usuarioId);
        return d;
    }

    private Usuario usuario(Long id, String nombre, String apellido) {
        return Usuario.builder().id(id).nombre(nombre).apellido(apellido)
                .email(nombre.toLowerCase() + "@mail.com").estado(EstadoUsuario.VALIDADO).build();
    }
}
