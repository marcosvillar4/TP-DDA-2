package com.example.DA2Back.producto.dato;

import com.example.DA2Back.comercio.dato.Comercio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "productos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_producto_comercio_sku",
                        columnNames = {"comercio_id", "sku"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;
}
