package com.example.DA2Back.producto.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dto.ProductoCreateDTO;
import com.example.DA2Back.producto.dto.ProductoMapper;
import com.example.DA2Back.producto.dto.ProductoResponseDTO;
import com.example.DA2Back.producto.dto.ProductoUpdateDTO;
import com.example.DA2Back.producto.negocio.IProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final IProductoService productoService;

    public ProductoController(IProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> buscar(
            @RequestParam(required = false) Long comercioId,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String buscar) {

        List<ProductoResponseDTO> productos =
                productoService.buscar(comercioId, estado, categoria, buscar)
                        .stream()
                        .map(ProductoMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerPorComercio(
            @PathVariable Long comercioId) {

        List<ProductoResponseDTO> productos =
                productoService.obtenerPorComercio(comercioId)
                        .stream()
                        .map(ProductoMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(
            @PathVariable Long id) {

        Producto producto = productoService.obtenerPorId(id);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(producto)
        );
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @RequestBody ProductoCreateDTO dto) {

        Producto producto = ProductoMapper.toEntity(dto);

        Producto creado = productoService.crear(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductoMapper.toResponseDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProductoUpdateDTO dto) {

        Producto producto = ProductoMapper.toEntity(dto);

        Producto actualizado =
                productoService.actualizar(id, producto);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(actualizado)
        );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ProductoResponseDTO> activar(
            @PathVariable Long id) {

        Producto actualizado =
                productoService.activar(id);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(actualizado)
        );
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoResponseDTO> desactivar(
            @PathVariable Long id) {

        Producto desactivado =
                productoService.desactivar(id);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(desactivado)
        );
    }
}

