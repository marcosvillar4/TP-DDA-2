package com.example.DA2Back.Seguridad.negocio;

import com.example.DA2Back.Seguridad.dto.LoginDTO;
import com.example.DA2Back.Seguridad.dto.LoginResponseDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;

public interface IAuthService {
    
    UsuarioResponseDTO registrar(RegisterDTO dto);

    LoginResponseDTO login(LoginDTO loginDTO);

    /** Rota el refresh token y emite un nuevo access token. 401 si fue revocado. */
    LoginResponseDTO refrescar(String refreshToken);

    /** Cierra la sesión revocando el refresh token. */
    void logout(String refreshToken);
}
