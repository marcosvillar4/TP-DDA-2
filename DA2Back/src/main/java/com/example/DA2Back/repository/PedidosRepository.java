package com.example.DA2Back.repository;

import com.example.DA2Back.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Pedido.
 * Hereda operaciones CRUD y de paginacion de JpaRepository.
 */
@Repository
public interface PedidosRepository extends JpaRepository<Pedido, Long> {
}