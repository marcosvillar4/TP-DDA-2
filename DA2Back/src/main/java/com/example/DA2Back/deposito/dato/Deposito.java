package com.example.DA2Back.deposito.dato;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.inventario.dato.ItemInventario;

@Entity
@Table(name = "depositos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "comercio_id", nullable = false)
    private Comercio comercio;

    @OneToMany(mappedBy = "deposito")
    private List<ItemInventario> items;
}
