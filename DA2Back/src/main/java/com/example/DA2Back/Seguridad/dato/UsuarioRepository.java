package com.example.DA2Back.Seguridad.dato;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

}