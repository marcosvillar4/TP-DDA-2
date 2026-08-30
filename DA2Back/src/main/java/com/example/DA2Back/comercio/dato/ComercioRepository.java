package com.example.DA2Back.comercio.dato;

import com.example.DA2Back.comercio.dato.Comercio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Comercio.
 */
@Repository
public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    Optional<Comercio> findByEmail(String email);

    boolean existsByEmail(String email);
}