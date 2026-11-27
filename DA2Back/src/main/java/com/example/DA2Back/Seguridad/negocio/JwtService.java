package com.example.DA2Back.Seguridad.negocio;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** SEC-W02: vigencia maxima permitida del access token (15 minutos). */
    public static final long MAX_EXPIRATION_MS = 15L * 60 * 1000;

    /** B4: emisor esperado del access token (claim iss). */
    public static final String DEFAULT_ISSUER = "logired-api";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.issuer:" + DEFAULT_ISSUER + "}")
    private String issuer = DEFAULT_ISSUER;

    /**
     * SEC-W02 / B4: garantiza que ningun access token viva mas de 15 minutos
     * y que el issuer este definido.
     */
    @PostConstruct
    void validarExpiracion() {
        if (expiration == null || expiration <= 0) {
            throw new IllegalStateException("jwt.expiration debe ser un valor positivo en milisegundos");
        }
        if (expiration > MAX_EXPIRATION_MS) {
            log.warn("jwt.expiration={} ms supera el maximo permitido (SEC-W02); se limita a {} ms",
                    expiration, MAX_EXPIRATION_MS);
            expiration = MAX_EXPIRATION_MS;
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("jwt.issuer no puede estar vacío");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String getIssuer() {
        return issuer;
    }

    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()
                .issuer(issuer)
                .subject(userDetails.getUsername())
                .claim(
                        "rol",
                        userDetails.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority()
                )
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + expiration
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractIssuer(String token) {
        return extractAllClaims(token).getIssuer();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
