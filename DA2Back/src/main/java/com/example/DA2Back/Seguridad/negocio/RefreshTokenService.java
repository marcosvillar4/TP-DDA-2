package com.example.DA2Back.Seguridad.negocio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DA2Back.Seguridad.dato.RefreshToken;
import com.example.DA2Back.Seguridad.dato.RefreshTokenRepository;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.excepcion.RefreshTokenInvalidoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ciclo de vida de los refresh tokens (SEC-W03 — OWASP A07 / ASVS V3).
 *
 *  - emitir():  genera un token aleatorio opaco (256 bits) y persiste su hash.
 *  - rotar():   valida el token, lo revoca y emite uno nuevo (rotación).
 *               Si llega un token YA revocado se asume robo/reuso y se
 *               revocan todas las sesiones del usuario.
 *  - revocar(): logout. El token queda inutilizable para futuros refresh.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String MENSAJE_INVALIDO = "Refresh token inválido, revocado o vencido";

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;

    /** Vigencia del refresh token en ms (default: 7 días). */
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    /** Resultado de una rotación: el usuario dueño y el nuevo token en claro. */
    public record Rotacion(Usuario usuario, String nuevoRefreshToken) {}

    @Transactional
    public String emitir(Usuario usuario) {
        String tokenPlano = generarTokenAleatorio();
        Instant ahora = Instant.now();

        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hash(tokenPlano))
                .usuarioId(usuario.getId())
                .creadoEn(ahora)
                .expiraEn(ahora.plusMillis(refreshExpirationMs))
                .revocado(false)
                .build());

        return tokenPlano;
    }

    /**
     * noRollbackFor: si se detecta reuso, la revocación masiva debe quedar
     * persistida aunque se lance la excepción que corta el request con 401.
     */
    @Transactional(noRollbackFor = RefreshTokenInvalidoException.class)
    public Rotacion rotar(String tokenPlano) {
        if (tokenPlano != null) tokenPlano = tokenPlano.trim();
        RefreshToken actual = buscarPorTokenPlano(tokenPlano);

        if (actual.isRevocado()) {
            int revocados = refreshTokenRepository.revocarTodosDelUsuario(actual.getUsuarioId());
            log.warn("Reuso de refresh token revocado para usuarioId={}. Se revocaron {} sesiones activas.",
                    actual.getUsuarioId(), revocados);
            throw new RefreshTokenInvalidoException(MENSAJE_INVALIDO);
        }
        if (actual.estaVencido(Instant.now())) {
            actual.setRevocado(true);
            throw new RefreshTokenInvalidoException(MENSAJE_INVALIDO);
        }

        Usuario usuario = usuarioRepository.findById(actual.getUsuarioId())
                .orElseThrow(() -> new RefreshTokenInvalidoException(MENSAJE_INVALIDO));

        if (!usuario.isEnabled() || !usuario.isAccountNonLocked()) {
            // La cuenta fue bloqueada/dada de baja después del login.
            refreshTokenRepository.revocarTodosDelUsuario(usuario.getId());
            throw new RefreshTokenInvalidoException(MENSAJE_INVALIDO);
        }

        actual.setRevocado(true);
        return new Rotacion(usuario, emitir(usuario));
    }

    /**
     * Logout: revoca el refresh token. Es idempotente y no revela si el token
     * existía (un token desconocido simplemente no hace nada).
     */
    @Transactional
    public void revocar(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) return;
        tokenPlano = tokenPlano.trim();
        refreshTokenRepository.findByTokenHash(hash(tokenPlano))
                .ifPresent(token -> token.setRevocado(true));
    }

    // -------------------------------------------------------------------------

    private RefreshToken buscarPorTokenPlano(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) {
            log.warn("Intento de refresh con token nulo o en blanco");
            throw new RefreshTokenInvalidoException(MENSAJE_INVALIDO);
        }
        String h = hash(tokenPlano.trim());
        return refreshTokenRepository.findByTokenHash(h)
                .orElseThrow(() -> {
                    log.warn("No se encontró ningún refresh token para el hash {}", h);
                    return new RefreshTokenInvalidoException(MENSAJE_INVALIDO);
                });
    }

    private static String generarTokenAleatorio() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String tokenPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
