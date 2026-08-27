package com.example.DA2Back.service.impl;

import com.example.DA2Back.dto.usuario.LoginDTO;
import com.example.DA2Back.dto.usuario.UsuarioCreateDTO;
import com.example.DA2Back.dto.usuario.UsuarioResponseDTO;
import com.example.DA2Back.entites.Comercio;
import com.example.DA2Back.entites.Usuario;
import com.example.DA2Back.mapper.UsuarioMapper;
import com.example.DA2Back.repository.ComercioRepository;
import com.example.DA2Back.repository.UsuarioRepository;
import com.example.DA2Back.service.ServicioDeSeguridad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Implementacion de ServicioDeSeguridad (gestion de usuarios y autenticacion).
 */
@Service
@RequiredArgsConstructor
public class ServicioDeSeguridadImpl implements ServicioDeSeguridad {

    private final UsuarioRepository usuarioRepository;
    private final ComercioRepository comercioRepository;

    @Override
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario '" + dto.getUsername() + "' ya esta en uso");
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);

        if (dto.getComercioId() != null) {
            Comercio comercio = comercioRepository.findById(dto.getComercioId())
                    .orElseThrow(() -> new NoSuchElementException("Comercio con id=" + dto.getComercioId() + " no encontrado"));
            usuario.setComercio(comercio);
        }

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO login(LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

        if (!usuario.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Credenciales invalidas");
        }

        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario con id=" + id + " no encontrado"));
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}