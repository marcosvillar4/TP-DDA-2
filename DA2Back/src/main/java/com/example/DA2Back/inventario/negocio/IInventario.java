package com.example.DA2Back.inventario.negocio;

import java.util.List;

import com.example.DA2Back.inventario.dato.Inventario;

public interface IInventario {

    Inventario obtenerPorId(Long id);

    Inventario obtenerPorComercio(Long comercioId);

    List<Inventario> obtenerTodos();

    Inventario crear(Long comercioId);

    void eliminar(Long id);
}
