package com.example.DA2Back.inventario.negocio;

import com.example.DA2Back.inventario.dato.ItemInventario;

import java.util.List;

public interface IItemInventario {
    List<ItemInventario> obtenerTodos();

    ItemInventario obtenerPorId(Long id);

    List<ItemInventario> obtenerPorInventario(Long inventarioId);

    List<ItemInventario> obtenerPorDeposito(Long depositoId);

    List<ItemInventario> obtenerPorProducto(Long productoId);

    ItemInventario crear(
    Long inventarioId,
    Long productoId,
    Long depositoId,
    Integer cantidad
);

    ItemInventario actualizarCantidad(Long id, Integer cantidad);

    void eliminar(Long id);
}
