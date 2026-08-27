package com.example.DA2Back.service;

import com.example.DA2Back.dto.usuario.LoginDTO;
import com.example.DA2Back.dto.usuario.UsuarioCreateDTO;
import com.example.DA2Back.dto.usuario.UsuarioResponseDTO;
import com.example.DA2Back.entites.Rol;
import com.example.DA2Back.entites.Usuario;
import com.example.DA2Back.repository.ComercioRepository;
import com.example.DA2Back.repository.UsuarioRepository;
import com.example.DA2Back.service.impl.ServicioDeSeguridadImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioDeSeguridadTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComercioRepository comercioRepository;

    @InjectMocks
    private ServicioDeSeguridadImpl servicioDeSeguridad;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .username("admin")
                .password("secret123")
                .rol(Rol.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Debe registrar un usuario exitosamente")
    void testRegistrarUsuario_Exito() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setUsername("admin");
        dto.setPassword("secret123");
        dto.setRol(Rol.ADMIN);

        when(usuarioRepository.existsByUsername("admin")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioResponseDTO resultado = servicioDeSeguridad.registrarUsuario(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("admin", resultado.getUsername());
    }

    @Test
    @DisplayName("Debe realizar login exitoso")
    void testLogin_Exito() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("secret123");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioMock));

        UsuarioResponseDTO resultado = servicioDeSeguridad.login(dto);

        assertNotNull(resultado);
        assertEquals("admin", resultado.getUsername());
    }
}