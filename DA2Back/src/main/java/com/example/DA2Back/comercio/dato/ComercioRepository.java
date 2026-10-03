package com.example.DA2Back.comercio.dato;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    Optional<Comercio> findByUsuarioId(Long usuarioId);

    @Query("SELECT COUNT(c) > 0 FROM Comercio c WHERE c.CUIT = :cuit AND c.id <> :id")
    boolean existsOtroConCuit(@Param("cuit") String cuit, @Param("id") Long id);

    @Query("SELECT COUNT(c) > 0 FROM Comercio c WHERE LOWER(c.email) = LOWER(:email) AND c.id <> :id")
    boolean existsOtroConEmail(@Param("email") String email, @Param("id") Long id);
}
