package com.example.DA2Back.inventario.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dto.InventarioCreateDTO;
import com.example.DA2Back.inventario.dto.InventarioMapper;
import com.example.DA2Back.inventario.dto.InventarioResponseDTO;
import com.example.DA2Back.inventario.negocio.IInventario;

@RestController
@RequestMapping("/inventarios")
public class InventarioController {

    private final IInventario inventarioService;

    public InventarioController(IInventario inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> obtenerTodos() {

        List<InventarioResponseDTO> inventarios =
                inventarioService.obtenerTodos()
                        .stream()
                        .map(InventarioMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorComercio(
            @PathVariable Long comercioId) {

        Inventario inventario =
                inventarioService.obtenerPorComercio(comercioId);

        return ResponseEntity.ok(
                InventarioMapper.toResponseDTO(inventario)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorId(
            @PathVariable Long id) {

        Inventario inventario =
                inventarioService.obtenerPorId(id);

        return ResponseEntity.ok(
                InventarioMapper.toResponseDTO(inventario)
        );
    }

    @PostMapping
    public ResponseEntity<InventarioResponseDTO> crear(
            @RequestBody InventarioCreateDTO dto) {

        Inventario creado =
                inventarioService.crear(dto.getComercioId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(InventarioMapper.toResponseDTO(creado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        inventarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
