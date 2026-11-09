package com.example.DA2Back.pago.dato;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Bloquea la fila para que dos pagos simultaneos del mismo cobro no se procesen dos veces. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.id = :id")
    Optional<Pago> findByIdForUpdate(@Param("id") Long id);

    boolean existsByPedidoId(Long pedidoId);

    List<Pago> findByComercioIdOrderByFechaCreacionDesc(Long comercioId);

    List<Pago> findByComercioIdAndEstadoOrderByFechaCreacionDesc(Long comercioId, EstadoPago estado);

    List<Pago> findByPedidoIdIn(Collection<Long> pedidoIds);
}
