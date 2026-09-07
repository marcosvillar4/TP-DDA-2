package com.example.DA2Back.producto.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public Producto obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del producto no puede ser null"
            );
        }

        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el producto con ID: " + id
                ));
    }

    @Override
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Producto crear(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser null"
            );
        }

        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del producto no puede ser null"
            );
        }

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser null"
            );
        }

        Producto productoExistente = obtenerPorId(id);

        productoExistente.setNombre(producto.getNombre());
        productoExistente.setDescripcion(producto.getDescripcion());
        productoExistente.setPrecio(producto.getPrecio());

        return productoRepository.save(productoExistente);
    }

    @Override
    public void eliminar(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del producto no puede ser null"
            );
        }

        Producto producto = obtenerPorId(id);

        productoRepository.delete(producto);
    }
}

