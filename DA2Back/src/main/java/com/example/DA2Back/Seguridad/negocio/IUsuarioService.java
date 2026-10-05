package com.example.DA2Back.Seguridad.negocio;

import java.util.List;

import com.example.DA2Back.deposito.dto.AsociarDepositoDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.Seguridad.dto.ActualizarUsuarioAdminDTO;
import com.example.DA2Back.Seguridad.dto.CambiarPasswordAdminDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.RegistroDepositoDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;

public interface IUsuarioService {
    UsuarioResponseDTO registrar(RegisterDTO dto);
    /**
     * Da de alta un depósito (y su usuario responsable) vinculado al comercio
     * del usuario COMERCIO indicado.
     */
    DepositoResponseDTO registrarDepositoParaComercio(RegistroDepositoDTO dto, Long usuarioComercioId);
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO obtenerPorId(Long id);
    UsuarioResponseDTO obtenerPorEmail(String email);
    UsuarioResponseDTO actualizarUsuarioAdmin(Long id, ActualizarUsuarioAdminDTO dto);
    void resetearPasswordAdmin(Long id, CambiarPasswordAdminDTO dto);
    UsuarioResponseDTO validar(Long id);
    UsuarioResponseDTO rechazar(Long id);
    UsuarioResponseDTO bloquear(Long id);
    UsuarioResponseDTO desbloquear(Long id);
    void asociarDepositoAComercio(AsociarDepositoDTO dto);
    boolean existePorId(Long id);

}
