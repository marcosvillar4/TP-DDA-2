package com.example.DA2Back.inventario.dato;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {
    List<ItemInventario> findByInventarioId(Long inventarioId);

    List<ItemInventario> findByDepositoId(Long depositoId);

    List<ItemInventario> findByProductoId(Long productoId);

    Optional<ItemInventario> findByInventarioIdAndProductoIdAndDepositoId(
            Long inventarioId,
            Long productoId,
            Long depositoId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemInventario i where i.inventario.id = :inventarioId "
            + "and i.producto.id = :productoId and i.deposito.id = :depositoId")
    Optional<ItemInventario> buscarParaActualizarStock(@Param("inventarioId") Long inventarioId,
                                                     @Param("productoId") Long productoId,
                                                     @Param("depositoId") Long depositoId);

    @Query("""
            SELECT i
            FROM ItemInventario i
            WHERE i.cantidad <= COALESCE(i.stockMinimo, 5)
            """)
    List<ItemInventario> findAlertasStock();

    @Query("""
            SELECT i
            FROM ItemInventario i
            WHERE i.inventario.comercio.id = :comercioId
              AND i.cantidad <= COALESCE(i.stockMinimo, 5)
            """)
    List<ItemInventario> findAlertasStockByComercioId(@Param("comercioId") Long comercioId);

    @Query("""
            SELECT i
            FROM ItemInventario i
            WHERE i.deposito.id = :depositoId
              AND i.cantidad <= COALESCE(i.stockMinimo, 5)
            """)
    List<ItemInventario> findAlertasStockByDepositoId(@Param("depositoId") Long depositoId);
}
