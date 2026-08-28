package com.example.DA2Back.Seguridad;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

import com.example.DA2Back.usuario.Usuario;
import com.example.DA2Back.usuario.usuarioDTOs.LoginDTO;
import com.example.DA2Back.usuario.usuarioDTOs.LoginResponseDTO;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginDTO loginDTO) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(),
                                loginDTO.getPassword()
                        )
                );

        Usuario usuario = (Usuario) authentication.getPrincipal();

        String token = jwtService.generateToken(usuario);

        Long comercioId = null;

        if (usuario.getComercio() != null) {
            comercioId = usuario.getComercio().getId();
        }

        return new LoginResponseDTO(
                token,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                comercioId
        );
    }
}
