package com.example.DA2Back.pedidos.dato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Pedido.
 * Hereda operaciones CRUD y de paginacion de JpaRepository.
 */
@Repository
public interface PedidosRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByComercioId(Long comercioId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByEstadoAndRepartidorIsNull(EstadoPedido estado);

    List<Pedido> findByRepartidorIdAndEstadoIn(Long repartidorId, List<EstadoPedido> estados);

    Optional<Pedido> findFirstByRepartidorIdAndEstadoIn(Long repartidorId, List<EstadoPedido> estados);

    boolean existsByRepartidorIdAndEstadoIn(Long repartidorId, List<EstadoPedido> estados);
}
