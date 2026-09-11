package com.example.DA2Back.Seguridad.negocio;

import java.util.List;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Service;

import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.deposito.dto.AsociarDepositoDTO;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.Seguridad.excepcion.UsuarioYaExisteException;
import com.example.DA2Back.Seguridad.negocio.State.ResolverEstadoUsuario;
import com.example.DA2Back.Seguridad.negocio.State.IEstadoUsuario;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioFactory usuarioFactory;
    private final ResolverEstadoUsuario resolverEstadoUsuario;
    private final IDeposito depositoService;

    @Override
    @Transactional
    public UsuarioResponseDTO registrar(RegisterDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new UsuarioYaExisteException("Ya existe un usuario con ese email");
        }

        Usuario usuario = usuarioFactory.crearDesdeRegistro(dto);
        Usuario guardado = usuarioRepository.save(usuario);

        usuarioFactory.crearEntidadRelacionada(dto, guardado.getId());

        return UsuarioMapper.toResponseDTO(guardado);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toResponseDTO)
                .toList();
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO validar(Long id) {
        return aplicarTransicion(id, IEstadoUsuario::validar);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO rechazar(Long id) {
        return aplicarTransicion(id, IEstadoUsuario::rechazar);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO bloquear(Long id) {
        return aplicarTransicion(id, IEstadoUsuario::bloquear);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO desbloquear(Long id) {
        return aplicarTransicion(id, IEstadoUsuario::desbloquear);
    }

    @Override
    @Transactional
    public void asociarDepositoAComercio(AsociarDepositoDTO dto) {
        depositoService.asociarAComercio(dto.getDepositoId(), dto.getComercioId());
    }

    public boolean existePorId(Long id) {
        return usuarioRepository.existsById(id);
    }

    private UsuarioResponseDTO aplicarTransicion(Long id, BiConsumer<IEstadoUsuario, Usuario> transicion) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));

        IEstadoUsuario estadoActual = resolverEstadoUsuario.resolver(usuario.getEstado());
        transicion.accept(estadoActual, usuario);

        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }
}
