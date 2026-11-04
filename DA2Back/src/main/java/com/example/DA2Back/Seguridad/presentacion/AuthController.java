package com.example.DA2Back.Seguridad.presentacion;

import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.Seguridad.dto.LoginDTO;
import com.example.DA2Back.Seguridad.dto.LoginResponseDTO;
import com.example.DA2Back.Seguridad.dto.RefreshTokenRequestDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.negocio.IAuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<UsuarioResponseDTO> register(
        @Valid @RequestBody RegisterDTO registerDTO
    ) {
        UsuarioResponseDTO creado = authService.registrar(registerDTO);

        return ResponseEntity.status(
            HttpStatus.CREATED).body(creado
        );
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginDTO loginDTO
    ) {
        return ResponseEntity.ok(
                authService.login(loginDTO)
        );
    }

    /**
     * Renueva el access token. Rota el refresh token: el enviado queda
     * revocado y se devuelve uno nuevo. 401 si está revocado o vencido.
     */
    @PostMapping({"/auth/refresh", "/refresh"})
    public ResponseEntity<LoginResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        return ResponseEntity.ok(authService.refrescar(request.getRefreshToken()));
    }

    /** Cierra la sesión revocando el refresh token (SEC-W03). */
    @PostMapping({"/auth/logout", "/logout"})
    public ResponseEntity<Map<String, String>> logout(
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Sesión cerrada correctamente. El refresh token ha sido revocado."
        ));
    }
}
