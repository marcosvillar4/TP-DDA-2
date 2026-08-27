package com.example.DA2Back.service;

import com.example.DA2Back.dto.usuario.LoginDTO;
import com.example.DA2Back.dto.usuario.UsuarioCreateDTO;
import com.example.DA2Back.dto.usuario.UsuarioResponseDTO;

import java.util.List;

/**
 * Interfaz del componente ServicioDeSeguridad
 * segun el diagrama de arquitectura LogiRed.
 */
public interface ServicioDeSeguridad {

    UsuarioResponseDTO registrarUsuario(UsuarioCreateDTO dto);

    UsuarioResponseDTO login(LoginDTO dto);

    UsuarioResponseDTO obtenerPorId(Long id);

    List<UsuarioResponseDTO> listarTodos();
}