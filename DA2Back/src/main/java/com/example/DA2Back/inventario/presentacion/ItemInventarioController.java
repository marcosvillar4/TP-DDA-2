package com.example.DA2Back.inventario.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dto.ItemInventarioCreateDTO;
import com.example.DA2Back.inventario.dto.ItemInventarioMapper;
import com.example.DA2Back.inventario.dto.ItemInventarioResponseDTO;
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
                dto.getCantidad()
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        itemInventarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}