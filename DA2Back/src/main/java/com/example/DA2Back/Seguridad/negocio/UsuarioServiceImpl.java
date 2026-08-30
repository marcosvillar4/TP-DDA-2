package com.example.DA2Back.Seguridad.negocio;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.Seguridad.excepcion.UsuarioYaExisteException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioFactory usuarioFactory;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioResponseDTO registrar(RegisterDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new UsuarioYaExisteException("Ya existe un usuario con ese email");
        }

        Usuario usuario = usuarioFactory.crearDesdeRegistro(dto, passwordEncoder);
        Usuario guardado = usuarioRepository.save(usuario);

        return toResponseDTO(guardado);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        return toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));
        return toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        usuario.setActivo(activo);
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .build();
    }
}
