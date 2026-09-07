package com.example.DA2Back.producto.negocio;

import java.util.List;

import com.example.DA2Back.producto.dato.Producto;

public interface IProductoService {

    Producto obtenerPorId(Long id);

    List<Producto> obtenerTodos();

    Producto crear(Producto producto);

    Producto actualizar(Long id, Producto producto);

    void eliminar(Long id);
}

