package com.example.DA2Back.inventario.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dto.AlertaStockDTO;
import com.example.DA2Back.inventario.dto.ItemInventarioCreateDTO;
import com.example.DA2Back.inventario.dto.ItemInventarioMapper;
import com.example.DA2Back.inventario.dto.ItemInventarioResponseDTO;
import com.example.DA2Back.inventario.dto.StockMinimoDTO;
import com.example.DA2Back.inventario.negocio.IItemInventario;

@RestController
@RequestMapping("/items-inventario")
public class ItemInventarioController {

    private final IItemInventario itemInventarioService;

    public ItemInventarioController(
            IItemInventario itemInventarioService) {

        this.itemInventarioService = itemInventarioService;
    }

    @GetMapping
    public ResponseEntity<List<ItemInventarioResponseDTO>> obtenerTodos() {

        List<ItemInventarioResponseDTO> items =
                itemInventarioService.obtenerTodos()
                        .stream()
                        .map(ItemInventarioMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/alertas-stock")
    public ResponseEntity<List<AlertaStockDTO>> obtenerAlertasStock(
            @AuthenticationPrincipal Usuario usuario) {

        List<AlertaStockDTO> alertas =
                itemInventarioService.obtenerAlertasStock(usuario)
                        .stream()
                        .map(this::toAlertaStockDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemInventarioResponseDTO> obtenerPorId(
            @PathVariable Long id) {

        ItemInventario item =
                itemInventarioService.obtenerPorId(id);

        return ResponseEntity.ok(
                ItemInventarioMapper.toResponseDTO(item)
        );
    }

    @GetMapping("/inventario/{inventarioId}")
    public ResponseEntity<List<ItemInventarioResponseDTO>> obtenerPorInventario(
            @PathVariable Long inventarioId) {

        List<ItemInventarioResponseDTO> items =
                itemInventarioService
                        .obtenerPorInventario(inventarioId)
                        .stream()
                        .map(ItemInventarioMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/deposito/{depositoId}")
    public ResponseEntity<List<ItemInventarioResponseDTO>> obtenerPorDeposito(
            @PathVariable Long depositoId) {

        List<ItemInventarioResponseDTO> items =
                itemInventarioService
                        .obtenerPorDeposito(depositoId)
                        .stream()
                        .map(ItemInventarioMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ItemInventarioResponseDTO>> obtenerPorProducto(
            @PathVariable Long productoId) {

        List<ItemInventarioResponseDTO> items =
                itemInventarioService
                        .obtenerPorProducto(productoId)
                        .stream()
                        .map(ItemInventarioMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<ItemInventarioResponseDTO> crear(
            @RequestBody ItemInventarioCreateDTO dto) {

        ItemInventario creado =
                itemInventarioService.crear(
                        dto.getInventarioId(),
                        dto.getProductoId(),
                        dto.getDepositoId(),
                        dto.getCantidad(),
                        dto.getStockMinimo()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ItemInventarioMapper.toResponseDTO(creado));
    }

    @PutMapping("/{id}/cantidad")
    public ResponseEntity<ItemInventarioResponseDTO> actualizarCantidad(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {

        ItemInventario actualizado =
                itemInventarioService.actualizarCantidad(
                        id,
                        cantidad
                );

        return ResponseEntity.ok(
                ItemInventarioMapper.toResponseDTO(actualizado)
        );
    }

    @PatchMapping("/{id}/stock-minimo")
    public ResponseEntity<ItemInventarioResponseDTO> actualizarStockMinimo(
            @PathVariable Long id,
            @RequestBody StockMinimoDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        ItemInventario actualizado =
                itemInventarioService.actualizarStockMinimo(
                        id,
                        dto != null ? dto.getStockMinimo() : null,
                        usuario
                );

        return ResponseEntity.ok(
                ItemInventarioMapper.toResponseDTO(actualizado)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        itemInventarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemInventarioResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ItemInventarioCreateDTO dto) {

        ItemInventario actualizado =
                itemInventarioService.actualizar(
                        id,
                        dto.getProductoId(),
                        dto.getDepositoId(),
                        dto.getCantidad()
                );

        return ResponseEntity.ok(
                ItemInventarioMapper.toResponseDTO(actualizado)
        );
    }

    private AlertaStockDTO toAlertaStockDTO(ItemInventario item) {
        return AlertaStockDTO.builder()
                .itemInventarioId(item.getId())
                .productoId(item.getProducto() != null ? item.getProducto().getId() : null)
                .productoNombre(item.getProducto() != null ? item.getProducto().getNombre() : null)
                .productoSku(item.getProducto() != null ? item.getProducto().getSku() : null)
                .depositoId(item.getDeposito() != null ? item.getDeposito().getId() : null)
                .depositoNombre(item.getDeposito() != null ? item.getDeposito().getNombre() : null)
                .comercioId(item.getInventario() != null && item.getInventario().getComercio() != null
                        ? item.getInventario().getComercio().getId()
                        : null)
                .comercioNombre(item.getInventario() != null && item.getInventario().getComercio() != null
                        ? item.getInventario().getComercio().getNombreComercial()
                        : null)
                .cantidad(item.getCantidad())
                .stockMinimo(item.getStockMinimo() != null ? item.getStockMinimo() : 5)
                .build();
    }
}
