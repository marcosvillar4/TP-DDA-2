package com.example.DA2Back.inventario.dato;

import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.producto.dato.Producto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "items_inventario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"inventario_id", "producto_id", "deposito_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "deposito_id", nullable = false)
    private Deposito deposito;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_minimo")
    private Integer stockMinimo = 5;

    @PrePersist
    @PreUpdate
    private void aplicarStockMinimoDefault() {
        if (stockMinimo == null) {
            stockMinimo = 5;
        }
    }
}
