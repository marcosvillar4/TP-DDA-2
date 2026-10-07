package com.example.DA2Back.inventario.negocio;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.negocio.IProductoService;

@Service
public class ItemInventarioService implements IItemInventario {

    private static final int STOCK_MINIMO_DEFAULT = 5;

    private final ItemInventarioRepository itemInventarioRepository;
    private final IInventario inventarioService;
    private final IProductoService productoService;
    private final IDeposito depositoService;
    private final IComercio comercioService;

    public ItemInventarioService(
            ItemInventarioRepository itemInventarioRepository,
            IInventario inventarioService,
            IProductoService productoService,
            IDeposito depositoService,
            IComercio comercioService) {

        this.itemInventarioRepository = itemInventarioRepository;
        this.inventarioService = inventarioService;
        this.productoService = productoService;
        this.depositoService = depositoService;
        this.comercioService = comercioService;
    }

    @Override
    public ItemInventario obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del item de inventario no puede ser nulo"
            );
        }

        return itemInventarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
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

        return crear(inventarioId, productoId, depositoId, cantidad, null);
    }

    @Override
    public ItemInventario crear(
            Long inventarioId,
            Long productoId,
            Long depositoId,
            Integer cantidad,
            Integer stockMinimo) {

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

        Integer stockMinimoFinal = normalizarStockMinimoOpcional(stockMinimo);

        Inventario inventario =
                inventarioService.obtenerPorId(inventarioId);

        Producto producto =
                productoService.obtenerPorId(productoId);

        Deposito deposito =
                depositoService.obtenerEntidadPorId(depositoId);

        validarMismoComercio(inventario, producto, deposito);

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
                    nuevo.setStockMinimo(stockMinimoFinal);

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
    public ItemInventario actualizarStockMinimo(
            Long id,
            Integer stockMinimo,
            Usuario usuario) {

        validarUsuarioAutenticado(usuario);
        validarStockMinimoObligatorio(stockMinimo);

        ItemInventario item = obtenerPorId(id);
        validarPuedeConfigurarStockMinimo(item, usuario);

        item.setStockMinimo(stockMinimo);

        return itemInventarioRepository.save(item);
    }

    @Override
    public List<ItemInventario> obtenerAlertasStock(Usuario usuario) {
        validarUsuarioAutenticado(usuario);

        if (usuario.getRol() == Rol.ADMIN) {
            return itemInventarioRepository.findAlertasStock();
        }

        if (usuario.getRol() == Rol.COMERCIO) {
            Long comercioId = comercioService.obtenerPorUsuarioId(usuario.getId()).getId();
            return itemInventarioRepository.findAlertasStockByComercioId(comercioId);
        }

        if (usuario.getRol() == Rol.DEPOSITO) {
            DepositoResponseDTO deposito = depositoService.obtenerPorUsuarioId(usuario.getId());
            return itemInventarioRepository.findAlertasStockByDepositoId(deposito.getId());
        }

        throw new AccessDeniedException("No tenés permisos para consultar alertas de inventario");
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

        @Override
    public ItemInventario actualizar(
            Long id,
            Long productoId,
            Long depositoId,
            Integer cantidad) {

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

        Producto productoFinal = productoId != null
                ? productoService.obtenerPorId(productoId)
                : item.getProducto();

        Deposito depositoFinal = depositoId != null
                ? depositoService.obtenerEntidadPorId(depositoId)
                : item.getDeposito();

        validarMismoComercio(item.getInventario(), productoFinal, depositoFinal);
        validarCombinacionNoDuplicada(item, productoFinal, depositoFinal);

        item.setProducto(productoFinal);
        item.setDeposito(depositoFinal);
        item.setCantidad(cantidad);

        return itemInventarioRepository.save(item);
    }

    private Integer normalizarStockMinimoOpcional(Integer stockMinimo) {
        if (stockMinimo == null) {
            return STOCK_MINIMO_DEFAULT;
        }

        validarStockMinimoObligatorio(stockMinimo);
        return stockMinimo;
    }

    private void validarStockMinimoObligatorio(Integer stockMinimo) {
        if (stockMinimo == null) {
            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser nulo"
            );
        }

        if (stockMinimo < 0) {
            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser negativo"
            );
        }
    }

    private void validarUsuarioAutenticado(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || usuario.getRol() == null) {
            throw new AccessDeniedException("Usuario autenticado inválido");
        }
    }

    private void validarPuedeConfigurarStockMinimo(ItemInventario item, Usuario usuario) {
        if (usuario.getRol() == Rol.ADMIN) {
            return;
        }

        if (usuario.getRol() == Rol.COMERCIO) {
            Long usuarioComercioId = comercioService.obtenerPorUsuarioId(usuario.getId()).getId();
            Long itemComercioId = obtenerComercioId(item.getInventario().getComercio());

            if (Objects.equals(usuarioComercioId, itemComercioId)) {
                return;
            }
        }

        throw new AccessDeniedException("No tenés permisos para configurar el stock mínimo de este item");
    }

    private void validarMismoComercio(
            Inventario inventario,
            Producto producto,
            Deposito deposito) {

        Long comercioInventarioId = obtenerComercioId(
                inventario != null ? inventario.getComercio() : null
        );
        Long comercioProductoId = obtenerComercioId(
                producto != null ? producto.getComercio() : null
        );
        Long comercioDepositoId = obtenerComercioId(
                deposito != null ? deposito.getComercio() : null
        );

        if (comercioInventarioId == null) {
            throw new IllegalStateException(
                    "El inventario no tiene un comercio asociado"
            );
        }

        if (!Objects.equals(comercioInventarioId, comercioProductoId)) {
            throw new IllegalStateException(
                    "El producto no pertenece al comercio del inventario"
            );
        }

        if (!Objects.equals(comercioInventarioId, comercioDepositoId)) {
            throw new IllegalStateException(
                    "El depósito no pertenece al comercio del inventario"
            );
        }
    }

    private Long obtenerComercioId(Comercio comercio) {
        return comercio != null ? comercio.getId() : null;
    }

    private void validarCombinacionNoDuplicada(
            ItemInventario item,
            Producto productoFinal,
            Deposito depositoFinal) {

        itemInventarioRepository
                .findByInventarioIdAndProductoIdAndDepositoId(
                        item.getInventario().getId(),
                        productoFinal.getId(),
                        depositoFinal.getId()
                )
                .filter(itemExistente -> !Objects.equals(
                        itemExistente.getId(),
                        item.getId()
                ))
                .ifPresent(itemExistente -> {
                    throw new IllegalStateException(
                            "Ya existe un item de inventario para ese producto y depósito"
                    );
                });
    }
}

