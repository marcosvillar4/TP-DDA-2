package com.example.DA2Back.producto.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
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
    private final IComercio comercioService;

    public ProductoController(IProductoService productoService, IComercio comercioService) {
        this.productoService = productoService;
        this.comercioService = comercioService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> buscar(
            @RequestParam(required = false) Long comercioId,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String buscar,
            @AuthenticationPrincipal Usuario usuario) {

        if (comercioId != null) {
            validarPropiedadComercio(comercioId, usuario);
        }

        List<ProductoResponseDTO> productos =
                productoService.buscar(comercioId, estado, categoria, buscar)
                        .stream()
                        .filter(p -> puedeAccederProducto(p, usuario))
                        .map(ProductoMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerPorComercio(
            @PathVariable Long comercioId,
            @AuthenticationPrincipal Usuario usuario) {

        validarPropiedadComercio(comercioId, usuario);

        List<ProductoResponseDTO> productos =
                productoService.obtenerPorComercio(comercioId)
                        .stream()
                        .map(ProductoMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        Producto producto = productoService.obtenerPorId(id);
        validarPropiedadProducto(producto, usuario);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(producto)
        );
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @RequestBody ProductoCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        if (dto != null && dto.getComercioId() != null) {
            validarPropiedadComercio(dto.getComercioId(), usuario);
        }

        Producto producto = ProductoMapper.toEntity(dto);

        Producto creado = productoService.crear(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductoMapper.toResponseDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ProductoUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        Producto existente = productoService.obtenerPorId(id);
        validarPropiedadProducto(existente, usuario);

        Producto producto = ProductoMapper.toEntity(dto);

        Producto actualizado =
                productoService.actualizar(id, producto);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(actualizado)
        );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ProductoResponseDTO> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        Producto existente = productoService.obtenerPorId(id);
        validarPropiedadProducto(existente, usuario);

        Producto actualizado =
                productoService.activar(id);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(actualizado)
        );
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoResponseDTO> desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        Producto existente = productoService.obtenerPorId(id);
        validarPropiedadProducto(existente, usuario);

        Producto desactivado =
                productoService.desactivar(id);

        return ResponseEntity.ok(
                ProductoMapper.toResponseDTO(desactivado)
        );
    }

    /**
     * SEC-W06: Valida en el servidor que un usuario con rol COMERCIO solo pueda
     * operar sobre productos pertenecientes a su propio comercio.
     */
    private void validarPropiedadComercio(Long comercioId, Usuario usuario) {
        if (usuario != null && usuario.getRol() == Rol.COMERCIO) {
            ComercioResponseDTO comercio = comercioService.obtenerPorId(comercioId);
            if (!usuario.getId().equals(comercio.getUsuarioId())) {
                throw new AccessDeniedException(
                        "No tenés permisos para operar sobre productos de otro comercio");
            }
        }
    }

    private void validarPropiedadProducto(Producto producto, Usuario usuario) {
        if (!puedeAccederProducto(producto, usuario)) {
            throw new AccessDeniedException(
                    "No tenés permisos para operar sobre productos de otro comercio");
        }
    }

    private boolean puedeAccederProducto(Producto producto, Usuario usuario) {
        if (usuario == null || usuario.getRol() == Rol.ADMIN) {
            return true;
        }
        if (usuario.getRol() == Rol.COMERCIO && producto != null && producto.getComercio() != null) {
            return usuario.getId().equals(producto.getComercio().getUsuarioId());
        }
        return false;
    }
}
