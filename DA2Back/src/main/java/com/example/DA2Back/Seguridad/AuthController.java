package com.example.DA2Back.Seguridad;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.usuario.usuarioDTOs.LoginDTO;
import com.example.DA2Back.usuario.usuarioDTOs.LoginResponseDTO;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginDTO loginDTO
    ) {

        return ResponseEntity.ok(
                authService.login(loginDTO)
        );
    }
}
