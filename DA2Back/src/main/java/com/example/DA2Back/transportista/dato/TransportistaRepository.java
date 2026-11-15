package com.example.DA2Back.transportista.dato;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

    boolean existsByEmpresaIgnoreCase(String empresa);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transportista> findFirstByActivoTrueAndEstadoOrderByIdAsc(EstadoTransportista estado);
}
