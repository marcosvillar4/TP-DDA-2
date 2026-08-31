package com.example.DA2Back.Seguridad.presentacion;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.Seguridad.dto.LoginDTO;
import com.example.DA2Back.Seguridad.dto.LoginResponseDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.negocio.IAuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(
        @RequestBody RegisterDTO registerDTO
    ) {
        UsuarioResponseDTO creado = authService.registrar(registerDTO);
        
        return ResponseEntity.status(
            HttpStatus.CREATED).body(creado
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginDTO loginDTO
    ) {
        return ResponseEntity.ok(
                authService.login(loginDTO)
        );
    }
}
