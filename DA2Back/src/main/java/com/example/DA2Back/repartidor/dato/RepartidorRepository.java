package com.example.DA2Back.repartidor.dato;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Repartidor r where r.id = :id")
    Optional<Repartidor> findByIdForUpdate(@Param("id") Long id);

    Optional<Repartidor> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByPatenteIgnoreCase(String patente);

    // Candidatos para la asignacion automatica (consumidor de la cola de pedidos).
    List<Repartidor> findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor estado);
}
