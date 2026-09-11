package com.example.DA2Back.Seguridad.negocio;

import java.util.List;

import com.example.DA2Back.deposito.dto.AsociarDepositoDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;

public interface IUsuarioService {
    UsuarioResponseDTO registrar(RegisterDTO dto);
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO obtenerPorId(Long id);
    UsuarioResponseDTO obtenerPorEmail(String email);
    UsuarioResponseDTO validar(Long id);
    UsuarioResponseDTO rechazar(Long id);
    UsuarioResponseDTO bloquear(Long id);
    UsuarioResponseDTO desbloquear(Long id);
    void asociarDepositoAComercio(AsociarDepositoDTO dto);
    boolean existePorId(Long id);

}
