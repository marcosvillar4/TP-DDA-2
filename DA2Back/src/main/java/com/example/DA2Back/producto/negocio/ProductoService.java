package com.example.DA2Back.producto.negocio;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final IComercio comercioService;

    @Override
    public Producto obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del producto no puede ser null"
            );
        }

        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se encontró el producto con ID: " + id
                ));
    }

    @Override
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @Override
    public List<Producto> obtenerPorComercio(Long comercioId) {

        resolverComercio(comercioId);

        return productoRepository.findByComercioId(comercioId);
    }

    @Override
    public List<Producto> buscar(
            Long comercioId,
            EstadoProducto estado,
            String categoria,
            String buscar) {

        if (comercioId != null) {
            resolverComercio(comercioId);
        }

        return productoRepository.buscar(
                comercioId,
                estado,
                normalizarOpcional(categoria),
                normalizarOpcional(buscar)
        );
    }

    @Override
    public Producto crear(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser null"
            );
        }

        prepararProducto(producto, null);
        producto.setEstado(EstadoProducto.ACTIVO);

        return guardar(producto);
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

        validarCamposEditables(producto);
        productoExistente.setNombre(producto.getNombre());
        productoExistente.setDescripcion(producto.getDescripcion());
        productoExistente.setCategoria(producto.getCategoria());

        return guardar(productoExistente);
    }

    @Override
    public Producto activar(Long id) {
        return cambiarEstado(id, EstadoProducto.ACTIVO);
    }

    @Override
    public Producto desactivar(Long id) {
        return cambiarEstado(id, EstadoProducto.INACTIVO);
    }

    private void prepararProducto(Producto producto, Long idActual) {

        producto.setSku(requerirTexto(producto.getSku(), "El SKU es obligatorio"));
        validarCamposEditables(producto);

        Comercio comercio = resolverComercio(producto.getComercio());
        producto.setComercio(comercio);

        validarSkuUnico(comercio.getId(), producto.getSku(), idActual);
    }

    private void validarCamposEditables(Producto producto) {

        producto.setNombre(requerirTexto(producto.getNombre(), "El nombre del producto es obligatorio"));
        producto.setCategoria(requerirTexto(producto.getCategoria(), "La categoría del producto es obligatoria"));
        producto.setDescripcion(normalizarOpcional(producto.getDescripcion()));
    }

    private Producto cambiarEstado(Long id, EstadoProducto estado) {

        Producto producto = obtenerPorId(id);
        producto.setEstado(estado);

        return guardar(producto);
    }

    private Comercio resolverComercio(Comercio comercioProducto) {

        if (comercioProducto == null) {
            throw new IllegalArgumentException(
                    "El comercio del producto es obligatorio"
            );
        }

        return resolverComercio(comercioProducto.getId());
    }

    private Comercio resolverComercio(Long comercioId) {

        if (comercioId == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio es obligatorio"
            );
        }

        try {
            return comercioService.obtenerPorId(comercioId);
        } catch (RuntimeException ex) {
            throw new NoSuchElementException(
                    "No se encontró el comercio con ID: " + comercioId
            );
        }
    }

    private void validarSkuUnico(Long comercioId, String sku, Long idActual) {

        productoRepository.findByComercioIdAndSkuIgnoreCase(comercioId, sku)
                .filter(producto -> idActual == null || !producto.getId().equals(idActual))
                .ifPresent(producto -> {
                    throw new IllegalStateException(
                            "Ya existe un producto con SKU " + sku
                                    + " para el comercio indicado"
                    );
                });
    }

    private String requerirTexto(String valor, String mensaje) {

        String normalizado = normalizarOpcional(valor);

        if (normalizado == null) {
            throw new IllegalArgumentException(mensaje);
        }

        return normalizado;
    }

    private String normalizarOpcional(String valor) {

        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();

        return normalizado.isEmpty() ? null : normalizado;
    }

    private Producto guardar(Producto producto) {

        try {
            return productoRepository.save(producto);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Ya existe un producto con ese SKU para el comercio indicado"
            );
        }
    }
}

