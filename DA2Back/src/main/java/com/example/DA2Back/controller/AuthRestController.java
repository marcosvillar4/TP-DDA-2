package com.example.DA2Back.controller;

import com.example.DA2Back.dto.usuario.LoginDTO;
import com.example.DA2Back.dto.usuario.UsuarioCreateDTO;
import com.example.DA2Back.dto.usuario.UsuarioResponseDTO;
import com.example.DA2Back.service.ServicioDeSeguridad;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para Autenticacion y Gestion de Usuarios (Login/Registro).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final ServicioDeSeguridad servicioDeSeguridad;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO respuesta = servicioDeSeguridad.registrarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody LoginDTO dto) {
        UsuarioResponseDTO respuesta = servicioDeSeguridad.login(dto);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(servicioDeSeguridad.listarTodos());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDeSeguridad.obtenerPorId(id));
    }
}