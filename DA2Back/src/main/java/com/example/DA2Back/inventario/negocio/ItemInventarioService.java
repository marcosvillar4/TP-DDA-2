package com.example.DA2Back.inventario.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.negocio.IProductoService;

@Service
public class ItemInventarioService implements IItemInventario {

    private final ItemInventarioRepository itemInventarioRepository;
    private final IInventario inventarioService;
    private final IProductoService productoService;
    private final IDeposito depositoService;

    public ItemInventarioService(
            ItemInventarioRepository itemInventarioRepository,
            IInventario inventarioService,
            IProductoService productoService,
            IDeposito depositoService) {

        this.itemInventarioRepository = itemInventarioRepository;
        this.inventarioService = inventarioService;
        this.productoService = productoService;
        this.depositoService = depositoService;
    }

    @Override
    public ItemInventario obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del item de inventario no puede ser nulo"
            );
        }

        return itemInventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el item de inventario con ID: " + id
                ));
    }

    @Override
    public List<ItemInventario> obtenerTodos() {
        return itemInventarioRepository.findAll();
    }

    @Override
    public List<ItemInventario> obtenerPorInventario(Long inventarioId) {

        if (inventarioId == null) {
            throw new IllegalArgumentException(
                    "El ID del inventario no puede ser nulo"
            );
        }

        return itemInventarioRepository.findByInventarioId(inventarioId);
    }

    @Override
    public List<ItemInventario> obtenerPorDeposito(Long depositoId) {

        if (depositoId == null) {
            throw new IllegalArgumentException(
                    "El ID del depósito no puede ser nulo"
            );
        }

        return itemInventarioRepository.findByDepositoId(depositoId);
    }

    @Override
    public List<ItemInventario> obtenerPorProducto(Long productoId) {

        if (productoId == null) {
            throw new IllegalArgumentException(
                    "El ID del producto no puede ser nulo"
            );
        }

        return itemInventarioRepository.findByProductoId(productoId);
    }

    @Override
    public ItemInventario crear(
            Long inventarioId,
            Long productoId,
            Long depositoId,
            Integer cantidad) {

        if (inventarioId == null) {
            throw new IllegalArgumentException(
                    "El ID del inventario no puede ser nulo"
            );
        }

        if (productoId == null) {
            throw new IllegalArgumentException(
                    "El ID del producto no puede ser nulo"
            );
        }

        if (depositoId == null) {
            throw new IllegalArgumentException(
                    "El ID del depósito no puede ser nulo"
            );
        }

        if (cantidad == null) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser nula"
            );
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser negativa"
            );
        }

        Inventario inventario =
                inventarioService.obtenerPorId(inventarioId);

        Producto producto =
                productoService.obtenerPorId(productoId);

        Deposito deposito =
                depositoService.obtenerPorId(depositoId);

        return itemInventarioRepository
                .findByInventarioIdAndProductoIdAndDepositoId(
                        inventarioId,
                        productoId,
                        depositoId
                )
                .map(itemExistente -> {

                    itemExistente.setCantidad(
                            itemExistente.getCantidad() + cantidad
                    );

                    return itemInventarioRepository.save(itemExistente);
                })
                .orElseGet(() -> {

                    ItemInventario nuevo = new ItemInventario();

                    nuevo.setInventario(inventario);
                    nuevo.setProducto(producto);
                    nuevo.setDeposito(deposito);
                    nuevo.setCantidad(cantidad);

                    return itemInventarioRepository.save(nuevo);
                });
    }

    @Override
    public ItemInventario actualizarCantidad(
            Long id,
            Integer cantidad) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del item de inventario no puede ser nulo"
            );
        }

        if (cantidad == null) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser nula"
            );
        }

        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser negativa"
            );
        }

        ItemInventario item = obtenerPorId(id);

        item.setCantidad(cantidad);

        return itemInventarioRepository.save(item);
    }

    @Override
    public void eliminar(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del item de inventario no puede ser nulo"
            );
        }

        ItemInventario item = obtenerPorId(id);

        itemInventarioRepository.delete(item);
    }
}

