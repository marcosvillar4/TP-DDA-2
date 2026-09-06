package com.example.DA2Back.inventario.dato;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {
    List<ItemInventario> findByInventarioId(Long inventarioId);

    List<ItemInventario> findByDepositoId(Long depositoId);

    List<ItemInventario> findByProductoId(Long productoId);

    Optional<ItemInventario> findByInventarioIdAndProductoIdAndDepositoId(
            Long inventarioId,
            Long productoId,
            Long depositoId
    );
}
