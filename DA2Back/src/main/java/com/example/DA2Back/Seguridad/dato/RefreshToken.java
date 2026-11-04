package com.example.DA2Back.Seguridad.dato;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Refresh token persistido del lado del servidor (SEC-W03).
 *
 * Nunca se guarda el valor en claro: solo su hash SHA-256. Así, aunque se
 * filtre la tabla, los tokens no se pueden reutilizar.
 *
 * Un token deja de servir cuando:
 *  - se revoca explícitamente (logout),
 *  - se rota (cada refresh emite uno nuevo y revoca el anterior),
 *  - vence (expiraEn).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Hash SHA-256 (hex) del token entregado al cliente. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Instant creadoEn;

    @Column(nullable = false)
    private Instant expiraEn;

    @Column(nullable = false)
    private boolean revocado;

    public boolean estaVencido(Instant ahora) {
        return expiraEn.isBefore(ahora);
    }
}
