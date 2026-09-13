package com.example.DA2Back.producto.negocio;

import java.util.List;

import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;

public interface IProductoService {

    Producto obtenerPorId(Long id);

    List<Producto> obtenerTodos();

    List<Producto> obtenerPorComercio(Long comercioId);

    List<Producto> buscar(
            Long comercioId,
            EstadoProducto estado,
            String categoria,
            String buscar
    );

    Producto crear(Producto producto);

    Producto actualizar(Long id, Producto producto);

    Producto activar(Long id);

    Producto desactivar(Long id);
}

