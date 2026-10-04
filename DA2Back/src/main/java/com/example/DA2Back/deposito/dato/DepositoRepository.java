package com.example.DA2Back.deposito.dato;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    List<Deposito> findByComercioId(Long comercioId);

    long countByComercioId(Long comercioId);

    Optional<Deposito> findByUsuarioId(Long usuarioId);
}
