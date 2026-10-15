package com.example.DA2Back.pedidos.dato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Pedido.
 * Hereda operaciones CRUD y de paginacion de JpaRepository.
 */
@Repository
public interface PedidosRepository extends JpaRepository<Pedido, Long> {

    @Query("""
            select p from Pedido p
            where (:comercioId is null or p.comercioId = :comercioId)
              and (:depositoId is null or p.depositoId = :depositoId)
              and (:estado is null or p.estado = :estado)
              and (:sinRepartidor = false or p.repartidor is null)
            """)
    List<Pedido> buscarEnAlcance(@Param("comercioId") Long comercioId,
                               @Param("depositoId") Long depositoId,
                               @Param("estado") EstadoPedido estado,
                               @Param("sinRepartidor") boolean sinRepartidor);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id);

    List<Pedido> findByComercioId(Long comercioId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByEstadoAndRepartidorIsNull(EstadoPedido estado);

    List<Pedido> findByRepartidorIdAndEstadoIn(Long repartidorId, List<EstadoPedido> estados);

    Optional<Pedido> findFirstByRepartidorIdAndEstadoIn(Long repartidorId, List<EstadoPedido> estados);

    boolean existsByRepartidorIdAndEstadoIn(Long repartidorId, List<EstadoPedido> estados);
}
