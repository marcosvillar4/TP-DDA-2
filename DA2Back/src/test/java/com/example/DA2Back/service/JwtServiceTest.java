package com.example.DA2Back.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.negocio.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** SEC-W02: la vigencia del access token nunca supera los 15 minutos. */
class JwtServiceTest {

    private static final String SECRET = "MiSuperClaveSecreta123456789012345678901234567890";
    private static final long QUINCE_MIN_MS = 15L * 60 * 1000;

    private final Usuario usuario = Usuario.builder()
            .email("jwt.unit@logired.com")
            .password("x")
            .rol(Rol.COMERCIO)
            .estado(EstadoUsuario.VALIDADO)
            .build();

    private JwtService servicio(long expiracionMs) {
        JwtService jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", SECRET);
        ReflectionTestUtils.setField(jwt, "expiration", expiracionMs);
        ReflectionTestUtils.invokeMethod(jwt, "validarExpiracion");
        return jwt;
    }

    private Claims claimsDe(String token) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build().parseSignedClaims(token).getPayload();
    }

    @Test
    @DisplayName("Con jwt.expiration de 15 min el token vence a los 15 min")
    void respetaLaExpiracionConfigurada() {
        Claims claims = claimsDe(servicio(QUINCE_MIN_MS).generateToken(usuario));
        assertEquals(QUINCE_MIN_MS / 1000, (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000);
    }

    @Test
    @DisplayName("Si jwt.expiration supera 15 min (ej. 1 h) se limita a 15 min")
    void limitaExpiracionMayorA15Minutos() {
        long antes = System.currentTimeMillis();
        Claims claims = claimsDe(servicio(60L * 60 * 1000).generateToken(usuario));
        long vigenciaMs = claims.getExpiration().getTime() - antes;
        assertTrue(vigenciaMs <= QUINCE_MIN_MS + 1000, "vigencia real: " + vigenciaMs + " ms");
    }

    @Test
    @DisplayName("Una expiración nula o no positiva impide arrancar el servicio")
    void rechazaExpiracionInvalida() {
        assertThrows(Exception.class, () -> servicio(0));
        assertThrows(Exception.class, () -> servicio(-1));
    }

    @Test
    @DisplayName("isTokenValid acepta un token vigente del mismo usuario")
    void aceptaTokenVigente() {
        JwtService jwt = servicio(QUINCE_MIN_MS);
        assertTrue(jwt.isTokenValid(jwt.generateToken(usuario), usuario));
    }

    @Test
    @DisplayName("Un token vencido es rechazado por el parser")
    void rechazaTokenVencido() {
        JwtService jwt = servicio(QUINCE_MIN_MS);
        String vencido = Jwts.builder()
                .subject(usuario.getEmail())
                .issuedAt(new Date(System.currentTimeMillis() - 20 * 60_000L))
                .expiration(new Date(System.currentTimeMillis() - 60_000L))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwt.isTokenValid(vencido, usuario));
    }
}
