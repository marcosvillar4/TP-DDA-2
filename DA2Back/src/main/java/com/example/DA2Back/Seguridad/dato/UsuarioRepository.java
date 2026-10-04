package com.example.DA2Back.Seguridad.dato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.DNI = :dni")
    boolean existsPorDni(@Param("dni") String dni);

    Optional<Usuario> findByEmail(String email);

}
