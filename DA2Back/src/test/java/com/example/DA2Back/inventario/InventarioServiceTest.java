package com.example.DA2Back.inventario;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.InventarioRepository;
import com.example.DA2Back.inventario.negocio.InventarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private IComercio comercioService;

    @InjectMocks
    private InventarioService service;

    @Test
    @DisplayName("obtenerPorId: nulo -> IllegalArgument; inexistente -> error con el id; ok")
    void obtenerPorId() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(null));

        when(inventarioRepository.findById(9L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.obtenerPorId(9L));
        assertTrue(ex.getMessage().contains("9"));

        Inventario inv = new Inventario();
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        assertSame(inv, service.obtenerPorId(1L));
    }

    @Test
    @DisplayName("obtenerPorComercio: nulo, sin inventario y ok")
    void obtenerPorComercio() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorComercio(null));

        when(inventarioRepository.findByComercioId(9L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.obtenerPorComercio(9L));
        assertTrue(ex.getMessage().contains("9"));

        Inventario inv = new Inventario();
        when(inventarioRepository.findByComercioId(1L)).thenReturn(Optional.of(inv));
        assertSame(inv, service.obtenerPorComercio(1L));
    }

    @Test
    @DisplayName("obtenerTodos delega en el repositorio")
    void obtenerTodos() {
        Inventario inv = new Inventario();
        when(inventarioRepository.findAll()).thenReturn(List.of(inv));
        assertEquals(List.of(inv), service.obtenerTodos());
    }

    @Test
    @DisplayName("crear: comercio nulo, comercio que ya tiene inventario y alta correcta")
    void crear() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(null));

        when(inventarioRepository.findByComercioId(1L)).thenReturn(Optional.of(new Inventario()));
        assertThrows(RuntimeException.class, () -> service.crear(1L));
        verify(inventarioRepository, never()).save(any());

        Comercio comercio = Comercio.builder().id(2L).build();
        when(inventarioRepository.findByComercioId(2L)).thenReturn(Optional.empty());
        when(comercioService.obtenerEntidadPorId(2L)).thenReturn(comercio);
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario creado = service.crear(2L);

        assertSame(comercio, creado.getComercio());
    }

    @Test
    @DisplayName("eliminar: nulo, inexistente (no borra) y ok")
    void eliminar() {
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(null));

        when(inventarioRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.eliminar(9L));
        verify(inventarioRepository, never()).delete(any());

        Inventario inv = new Inventario();
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        service.eliminar(1L);
        verify(inventarioRepository).delete(inv);
    }
}
