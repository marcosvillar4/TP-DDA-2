package com.example.DA2Back.inventario.dato;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.DA2Back.comercio.dato.Comercio;

@Entity
@Table(name = "inventarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "comercio_id", nullable = false, unique = true)
    private Comercio comercio;

    @OneToMany(mappedBy = "inventario", cascade = CascadeType.ALL)
    private List<ItemInventario> items;
}
