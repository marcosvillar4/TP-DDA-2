package com.example.DA2Back.Seguridad.negocio;

import java.util.List;

import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;

public interface IUsuarioService {
    UsuarioResponseDTO registrar(RegisterDTO dto);

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO obtenerPorId(Long id);

    UsuarioResponseDTO obtenerPorEmail(String email);

    UsuarioResponseDTO cambiarEstado(Long id, boolean activo);
}
