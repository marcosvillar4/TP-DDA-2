package com.example.DA2Back.deposito.dato;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    List<Deposito> findByComercioId(Long comercioId);
}
