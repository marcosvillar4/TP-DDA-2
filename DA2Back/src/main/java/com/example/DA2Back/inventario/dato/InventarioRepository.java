package com.example.DA2Back.inventario.dato;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByComercioId(Long comercioId);
}
