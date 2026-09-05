package com.example.DA2Back.inventario.dato;

import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.producto.dato.Producto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_inventario")
@Data
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
}
