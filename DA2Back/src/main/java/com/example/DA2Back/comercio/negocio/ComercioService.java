package com.example.DA2Back.comercio.negocio;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.comercio.excepcion.*;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioMapper;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ComercioService implements IComercio {

    private final ComercioRepository comercioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public ComercioResponseDTO obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }

        Comercio comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));

        return aDTO(comercio);
    }

    @Override
    public ComercioResponseDTO obtenerPorUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }

        Comercio comercio = comercioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario " + usuarioId + " no tiene un comercio asociado"));

        return aDTO(comercio);
    }

    @Override
    public List<ComercioResponseDTO> obtenerTodos() {
        List<Comercio> comercios = comercioRepository.findAll();

        // Una sola consulta para traer a todos los dueños (evita N+1).
        Map<Long, Usuario> usuariosPorId = usuarioRepository
                .findAllById(comercios.stream().map(Comercio::getUsuarioId).toList())
                .stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        return comercios.stream()
                .map(c -> ComercioMapper.toResponseDTO(c, usuariosPorId.get(c.getUsuarioId())))
                .toList();
    }

    @Override
    @Transactional
    public ComercioResponseDTO crear(ComercioCreateDTO dto, Long usuarioId) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del comercio no pueden ser null");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }

        Comercio comercio = ComercioMapper.toEntity(dto, usuarioId);
        Comercio guardado = comercioRepository.save(comercio);

        return aDTO(guardado);
    }

    @Override
    @Transactional
    public ComercioResponseDTO actualizar(Long id, ComercioCreateDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del comercio no pueden ser null");
        }

        Comercio comercioExistente = comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));

        if (comercioRepository.existsOtroConCuit(dto.getCuit(), id)) {
            throw new IllegalStateException("Ya existe otro comercio con ese CUIT");
        }
        if (comercioRepository.existsOtroConEmail(dto.getEmail(), id)) {
            throw new IllegalStateException("Ya existe otro comercio con ese email");
        }

        comercioExistente.setNombreComercial(dto.getNombreComercial());
        comercioExistente.setRazonSocial(dto.getRazonSocial());
        comercioExistente.setDireccion(dto.getDireccion());
        comercioExistente.setCUIT(dto.getCuit());
        comercioExistente.setTelefono(dto.getTelefono());
        comercioExistente.setEmail(dto.getEmail());

        return aDTO(comercioRepository.save(comercioExistente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }

        Comercio comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));

        comercioRepository.delete(comercio);
    }

    @Override
    public Comercio obtenerEntidadPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }
        return comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));
    }

    @Override
    public boolean existePorId(Long id) {
        return id != null && comercioRepository.existsById(id);
    }

    private ComercioResponseDTO aDTO(Comercio comercio) {
        Usuario dueno = usuarioRepository.findById(comercio.getUsuarioId()).orElse(null);
        return ComercioMapper.toResponseDTO(comercio, dueno);
    }
}
