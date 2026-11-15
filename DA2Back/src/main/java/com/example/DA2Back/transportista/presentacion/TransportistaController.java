package com.example.DA2Back.transportista.presentacion;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.transportista.dto.CambiarEstadoTransportistaDTO;
import com.example.DA2Back.transportista.dto.TransportistaCreateDTO;
import com.example.DA2Back.transportista.dto.TransportistaResponseDTO;
import com.example.DA2Back.transportista.dto.TransportistaUpdateDTO;
import com.example.DA2Back.transportista.negocio.ServicioDeTransportistas;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transportistas")
@RequiredArgsConstructor
public class TransportistaController {

    private final ServicioDeTransportistas servicio;

    @GetMapping
    public ResponseEntity<List<TransportistaResponseDTO>> listar() {
        return ResponseEntity.ok(servicio.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportistaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TransportistaResponseDTO> crear(@RequestBody TransportistaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportistaResponseDTO> actualizar(
            @PathVariable Long id, @RequestBody TransportistaUpdateDTO dto) {
        return ResponseEntity.ok(servicio.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TransportistaResponseDTO> cambiarEstado(
            @PathVariable Long id, @RequestBody CambiarEstadoTransportistaDTO dto) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<TransportistaResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.desactivar(id));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<TransportistaResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.activar(id));
    }
}
