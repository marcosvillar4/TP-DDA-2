package com.example.DA2Back.inventario.presentacion;

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
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dto.InventarioCreateDTO;
import com.example.DA2Back.inventario.dto.InventarioMapper;
import com.example.DA2Back.inventario.dto.InventarioResponseDTO;
import com.example.DA2Back.inventario.negocio.IInventario;

@RestController
@RequestMapping("/inventarios")
public class InventarioController {

    private final IInventario inventarioService;
    private final IComercio comercioService;

    public InventarioController(IInventario inventarioService, IComercio comercioService) {
        this.inventarioService = inventarioService;
        this.comercioService = comercioService;
    }

    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> obtenerTodos(
            @AuthenticationPrincipal Usuario usuario) {

        List<InventarioResponseDTO> inventarios =
                inventarioService.obtenerTodos()
                        .stream()
                        .filter(inv -> puedeAccederInventario(inv, usuario))
                        .map(InventarioMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorComercio(
            @PathVariable Long comercioId,
            @AuthenticationPrincipal Usuario usuario) {

        validarPropiedadComercio(comercioId, usuario);

        Inventario inventario =
                inventarioService.obtenerPorComercio(comercioId);

        return ResponseEntity.ok(
                InventarioMapper.toResponseDTO(inventario)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        Inventario inventario =
                inventarioService.obtenerPorId(id);
        validarPropiedadInventario(inventario, usuario);

        return ResponseEntity.ok(
                InventarioMapper.toResponseDTO(inventario)
        );
    }

    @PostMapping
    public ResponseEntity<InventarioResponseDTO> crear(
            @RequestBody InventarioCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        if (dto != null && dto.getComercioId() != null) {
            validarPropiedadComercio(dto.getComercioId(), usuario);
        }

        Inventario creado =
                inventarioService.crear(dto.getComercioId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(InventarioMapper.toResponseDTO(creado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        Inventario inventario = inventarioService.obtenerPorId(id);
        validarPropiedadInventario(inventario, usuario);

        inventarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    private void validarPropiedadComercio(Long comercioId, Usuario usuario) {
        if (usuario != null && usuario.getRol() == Rol.COMERCIO) {
            ComercioResponseDTO comercio = comercioService.obtenerPorId(comercioId);
            if (!usuario.getId().equals(comercio.getUsuarioId())) {
                throw new AccessDeniedException(
                        "No tenés permisos para operar sobre el inventario de otro comercio");
            }
        }
    }

    private void validarPropiedadInventario(Inventario inventario, Usuario usuario) {
        if (!puedeAccederInventario(inventario, usuario)) {
            throw new AccessDeniedException(
                    "No tenés permisos para operar sobre el inventario de otro comercio");
        }
    }

    private boolean puedeAccederInventario(Inventario inventario, Usuario usuario) {
        if (usuario == null || usuario.getRol() == Rol.ADMIN) {
            return true;
        }
        if (usuario.getRol() == Rol.COMERCIO && inventario != null && inventario.getComercio() != null) {
            return usuario.getId().equals(inventario.getComercio().getUsuarioId());
        }
        return false;
    }
}
