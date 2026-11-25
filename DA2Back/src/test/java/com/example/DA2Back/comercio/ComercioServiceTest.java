package com.example.DA2Back.comercio;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.comercio.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.comercio.negocio.ComercioService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComercioServiceTest {

    @Mock private ComercioRepository comercioRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ComercioService service;

    @Test
    @DisplayName("obtenerPorId: nulo, inexistente y ok con responsable")
    void obtenerPorId() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(null));
        when(comercioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(404L));

        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercio(1L, 10L)));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L)));
        ComercioResponseDTO dto = service.obtenerPorId(1L);
        assertEquals("Urban Shoes", dto.getNombreComercial());
        assertEquals("Lucia Mora", dto.getResponsable());
        assertEquals(EstadoUsuario.VALIDADO, dto.getEstado());
    }

    @Test
    @DisplayName("Sin usuario dueño el DTO no trae responsable ni estado")
    void sinDueno() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercio(1L, 10L)));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());
        ComercioResponseDTO dto = service.obtenerPorId(1L);
        assertNull(dto.getResponsable());
        assertNull(dto.getEstado());
    }

    @Test
    @DisplayName("obtenerPorUsuarioId: nulo, sin comercio y ok")
    void obtenerPorUsuarioId() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorUsuarioId(null));
        when(comercioRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorUsuarioId(5L));
        when(comercioRepository.findByUsuarioId(10L)).thenReturn(Optional.of(comercio(1L, 10L)));
        assertEquals(1L, service.obtenerPorUsuarioId(10L).getId());
    }

    @Test
    @DisplayName("obtenerTodos resuelve los dueños en una sola consulta")
    void obtenerTodos() {
        when(comercioRepository.findAll()).thenReturn(List.of(comercio(1L, 10L), comercio(2L, 11L)));
        when(usuarioRepository.findAllById(anyCollection())).thenReturn(List.of(usuario(10L)));

        List<ComercioResponseDTO> res = service.obtenerTodos();

        assertEquals(2, res.size());
        assertEquals("Lucia Mora", res.get(0).getResponsable());
        assertNull(res.get(1).getResponsable());
    }

    @Test
    @DisplayName("crear: valida nulos y persiste copiando los campos del DTO")
    void crear() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(null, 10L));
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto("a@a.com", "20-1"), null));

        when(comercioRepository.save(any(Comercio.class))).thenAnswer(i -> {
            Comercio c = i.getArgument(0);
            c.setId(7L);
            return c;
        });
        ComercioResponseDTO res = service.crear(dto("nuevo@mail.com", "20-123"), 10L);

        assertEquals(7L, res.getId());
        assertEquals("20-123", res.getCuit());
        assertEquals(10L, res.getUsuarioId());
    }

    @Test
    @DisplayName("actualizar: entradas invalidas, inexistente, CUIT o email duplicado")
    void actualizar_errores() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(null, dto("a@a.com", "1")));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, null));

        when(comercioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(404L, dto("a@a.com", "1")));

        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercio(1L, 10L)));
        when(comercioRepository.existsOtroConCuit("20-999", 1L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.actualizar(1L, dto("ok@mail.com", "20-999")));

        when(comercioRepository.existsOtroConEmail("dup@mail.com", 1L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.actualizar(1L, dto("dup@mail.com", "20-111")));
        verify(comercioRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar exitoso modifica todos los campos")
    void actualizar_ok() {
        Comercio existente = comercio(1L, 10L);
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(comercioRepository.save(existente)).thenReturn(existente);

        ComercioResponseDTO res = service.actualizar(1L, dto("nuevo@mail.com", "20-777"));

        assertEquals("nuevo@mail.com", existente.getEmail());
        assertEquals("20-777", existente.getCUIT());
        assertEquals("20-777", res.getCuit());
    }

    @Test
    @DisplayName("eliminar: nulo, inexistente y ok")
    void eliminar() {
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(null));
        when(comercioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(404L));
        Comercio c = comercio(1L, 10L);
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(c));
        service.eliminar(1L);
        verify(comercioRepository).delete(c);
    }

    @Test
    @DisplayName("obtenerEntidadPorId y existePorId")
    void entidadYExistencia() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerEntidadPorId(null));
        when(comercioRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerEntidadPorId(404L));
        Comercio c = comercio(1L, 10L);
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(c));
        assertSame(c, service.obtenerEntidadPorId(1L));

        when(comercioRepository.existsById(1L)).thenReturn(true);
        assertTrue(service.existePorId(1L));
        assertFalse(service.existePorId(2L));
        assertFalse(service.existePorId(null));
    }

    // ───────────── helpers ─────────────

    private Comercio comercio(Long id, Long usuarioId) {
        return Comercio.builder().id(id).nombreComercial("Urban Shoes").razonSocial("Urban SA")
                .direccion("Calle 1").CUIT("20-000").telefono("111").email("urban@mail.com")
                .usuarioId(usuarioId).build();
    }

    private Usuario usuario(Long id) {
        return Usuario.builder().id(id).nombre("Lucia").apellido("Mora").estado(EstadoUsuario.VALIDADO).build();
    }

    private ComercioCreateDTO dto(String email, String cuit) {
        return new ComercioCreateDTO("Nuevo Nombre", "Razon SRL", "Calle 2", cuit, "222", email);
    }
}
