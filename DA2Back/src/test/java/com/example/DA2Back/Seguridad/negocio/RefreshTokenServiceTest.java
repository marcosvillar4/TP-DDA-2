package com.example.DA2Back.Seguridad.negocio;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.RefreshToken;
import com.example.DA2Back.Seguridad.dato.RefreshTokenRepository;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.excepcion.RefreshTokenInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** SEC-W03: rotacion, reuso y revocacion de refresh tokens. Package-private para usar hash(). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, usuarioRepository);
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 60_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ───────────── emitir ─────────────

    @Test
    @DisplayName("Emitir devuelve un token aleatorio de 256 bits y persiste SOLO su hash con la vigencia configurada")
    void emitir() {
        Instant antes = Instant.now();

        String token = service.emitir(usuario(7L, EstadoUsuario.VALIDADO));
        String otro = service.emitir(usuario(7L, EstadoUsuario.VALIDADO));

        assertEquals(43, token.length(), "32 bytes en base64url sin relleno");
        assertNotEquals(token, otro, "cada token es distinto");
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        RefreshToken guardado = captor.getAllValues().get(0);
        assertEquals(RefreshTokenService.hash(token), guardado.getTokenHash());
        assertNotEquals(token, guardado.getTokenHash(), "nunca se guarda el token en claro");
        assertEquals(64, guardado.getTokenHash().length());
        assertEquals(7L, guardado.getUsuarioId());
        assertFalse(guardado.isRevocado());
        assertEquals(Duration.ofMillis(60_000), Duration.between(guardado.getCreadoEn(), guardado.getExpiraEn()));
        assertFalse(guardado.getCreadoEn().isBefore(antes));
    }

    @Test
    @DisplayName("hash es SHA-256 hexadecimal determinista")
    void hashDeterminista() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                RefreshTokenService.hash("abc"));
        assertEquals(RefreshTokenService.hash("x"), RefreshTokenService.hash("x"));
    }

    // ───────────── rotar ─────────────

    @Test
    @DisplayName("Rotar con token nulo, en blanco o desconocido -> RefreshTokenInvalido")
    void rotar_invalidos() {
        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar(null));
        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("   "));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("desconocido"));
        verify(refreshTokenRepository, never()).revocarTodosDelUsuario(anyLong());
    }

    @Test
    @DisplayName("Reuso de un token YA revocado: se revocan todas las sesiones del usuario y se rechaza")
    void rotar_reuso() {
        RefreshToken revocado = token(7L, true, Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("t"))).thenReturn(Optional.of(revocado));

        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("t"));

        verify(refreshTokenRepository).revocarTodosDelUsuario(7L);
    }

    @Test
    @DisplayName("Token vencido: se marca revocado y se rechaza, sin emitir uno nuevo")
    void rotar_vencido() {
        RefreshToken vencido = token(7L, false, Instant.now().minusSeconds(1));
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("t"))).thenReturn(Optional.of(vencido));

        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("t"));

        assertTrue(vencido.isRevocado());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rotar con el usuario inexistente -> RefreshTokenInvalido")
    void rotar_usuarioInexistente() {
        RefreshToken vigente = token(7L, false, Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("t"))).thenReturn(Optional.of(vigente));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("t"));
        assertFalse(vigente.isRevocado());
    }

    @Test
    @DisplayName("Cuenta bloqueada o deshabilitada despues del login: se revocan todas las sesiones")
    void rotar_cuentaNoHabilitada() {
        RefreshToken vigente = token(7L, false, Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("t"))).thenReturn(Optional.of(vigente));

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(7L, EstadoUsuario.BLOQUEADO)));
        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("t"));

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(7L, EstadoUsuario.EN_EVALUACION)));
        assertThrows(RefreshTokenInvalidoException.class, () -> service.rotar("t"));

        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).revocarTodosDelUsuario(7L);
        assertFalse(vigente.isRevocado());
    }

    @Test
    @DisplayName("Rotar un token vigente lo revoca y emite uno nuevo distinto (tolera espacios alrededor)")
    void rotar_ok() {
        RefreshToken vigente = token(7L, false, Instant.now().plusSeconds(60));
        Usuario u = usuario(7L, EstadoUsuario.VALIDADO);
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("tok"))).thenReturn(Optional.of(vigente));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(u));

        RefreshTokenService.Rotacion res = service.rotar("  tok  ");

        assertTrue(vigente.isRevocado());
        assertSame(u, res.usuario());
        assertNotEquals("tok", res.nuevoRefreshToken());
        assertEquals(43, res.nuevoRefreshToken().length());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).revocarTodosDelUsuario(anyLong());
    }

    // ───────────── revocar ─────────────

    @Test
    @DisplayName("Revocar (logout): idempotente; nulo/en blanco o desconocido no hacen nada")
    void revocar_sinEfecto() {
        service.revocar(null);
        service.revocar("  ");
        verify(refreshTokenRepository, never()).findByTokenHash(anyString());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        service.revocar("desconocido");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Revocar marca el token como revocado (tolera espacios)")
    void revocar_ok() {
        RefreshToken t = token(7L, false, Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("tok"))).thenReturn(Optional.of(t));

        service.revocar(" tok ");

        assertTrue(t.isRevocado());
    }

    // ───────────── helpers ─────────────

    private RefreshToken token(Long usuarioId, boolean revocado, Instant expira) {
        return RefreshToken.builder().usuarioId(usuarioId).revocado(revocado)
                .creadoEn(Instant.now().minusSeconds(10)).expiraEn(expira).tokenHash("h").build();
    }

    private Usuario usuario(Long id, EstadoUsuario estado) {
        return Usuario.builder().id(id).rol(Rol.COMERCIO).estado(estado).email("u@mail.com").build();
    }
}
