package com.example.DA2Back.producto.dato;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByComercioIdAndSkuIgnoreCase(Long comercioId, String sku);

    List<Producto> findByComercioId(Long comercioId);

    List<Producto> findByEstado(EstadoProducto estado);

    @Query("""
            SELECT p
            FROM Producto p
            WHERE (:comercioId IS NULL OR p.comercio.id = :comercioId)
              AND (:estado IS NULL OR p.estado = :estado)
              AND (:categoria IS NULL OR LOWER(p.categoria) = LOWER(:categoria))
              AND (
                    :buscar IS NULL
                    OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                    OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  )
            """)
    List<Producto> buscar(
            @Param("comercioId") Long comercioId,
            @Param("estado") EstadoProducto estado,
            @Param("categoria") String categoria,
            @Param("buscar") String buscar
    );
}
