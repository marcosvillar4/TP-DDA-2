package com.example.DA2Back.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.repartidor.dto.EntregaHistorialDTO;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;
import com.example.DA2Back.repartidor.presentacion.RepartidorPerfilController;

@ExtendWith(MockitoExtension.class)
class RepartidorPerfilControllerTest {

    @Mock
    private IRepartidorService repartidorService;

    @InjectMocks
    private RepartidorPerfilController controller;

    private Usuario usuarioAutenticado;
    private RepartidorResponseDTO perfil;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = Usuario.builder()
                .id(1L)
                .email("repartidor@logired.com")
                .rol(Rol.REPARTIDOR)
                .build();

        perfil = RepartidorResponseDTO.builder()
                .id(10L)
                .usuarioId(1L)
                .nombreCompleto("Carlos Ruiz")
                .build();
    }

    @Test
    @DisplayName("GET /repartidor/me debe resolver perfil por usuario autenticado")
    void obtenerMiPerfil_resuelvePorUsuarioAutenticado() {
        when(repartidorService.obtenerPorUsuarioId(1L)).thenReturn(perfil);

        ResponseEntity<RepartidorResponseDTO> response = controller.obtenerMiPerfil(usuarioAutenticado);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10L, response.getBody().getId());
        assertEquals(1L, response.getBody().getUsuarioId());
        verify(repartidorService).obtenerPorUsuarioId(1L);
    }

    @Test
    @DisplayName("GET /repartidor/me/historial debe usar el repartidor del usuario autenticado")
    void obtenerMiHistorial_resuelveRepartidorDelUsuarioAutenticado() {
        EntregaHistorialDTO entrega = EntregaHistorialDTO.builder()
                .pedidoId(50L)
                .direccionEntrega("Av. Corrientes 1234")
                .build();

        when(repartidorService.obtenerPorUsuarioId(1L)).thenReturn(perfil);
        when(repartidorService.obtenerHistorial(10L)).thenReturn(List.of(entrega));

        ResponseEntity<List<EntregaHistorialDTO>> response =
                controller.obtenerMiHistorial(usuarioAutenticado);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(50L, response.getBody().get(0).getPedidoId());
        verify(repartidorService).obtenerPorUsuarioId(1L);
        verify(repartidorService).obtenerHistorial(10L);
    }
}
