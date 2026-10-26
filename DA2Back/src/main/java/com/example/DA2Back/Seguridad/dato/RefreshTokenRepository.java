package com.example.DA2Back.Seguridad.dato;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revoca todas las sesiones vigentes de un usuario (detección de reuso). */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.usuarioId = :usuarioId AND r.revocado = false")
    int revocarTodosDelUsuario(@Param("usuarioId") Long usuarioId);
}
