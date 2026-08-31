package com.example.DA2Back.service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.ServicioDeComerciosImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioDeComerciosTest {

    @Mock
    private ComercioRepository comercioRepository;

    @InjectMocks
    private ServicioDeComerciosImpl servicioDeComercios;

    private Comercio comercioMock;

    @BeforeEach
    void setUp() {
        comercioMock = Comercio.builder()
                .id(1L)
                .nombre("Pizzeria Test")
                .direccion("Av. Mayo 500")
                .telefono("1122334455")
                .email("contacto@pizzeria.com")
                .build();
    }

    @Test
    @DisplayName("Debe registrar un comercio exitosamente")
    void testRegistrar_Exito() {
        ComercioCreateDTO dto = new ComercioCreateDTO();
        dto.setNombre("Pizzeria Test");
        dto.setDireccion("Av. Mayo 500");
        dto.setTelefono("1122334455");
        dto.setEmail("contacto@pizzeria.com");

        when(comercioRepository.existsByEmail("contacto@pizzeria.com")).thenReturn(false);
        when(comercioRepository.save(any(Comercio.class))).thenReturn(comercioMock);

        ComercioResponseDTO resultado = servicioDeComercios.registrar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pizzeria Test", resultado.getNombre());
        verify(comercioRepository, times(1)).save(any(Comercio.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el email ya existe al registrar")
    void testRegistrar_EmailDuplicado() {
        ComercioCreateDTO dto = new ComercioCreateDTO();
        dto.setEmail("contacto@pizzeria.com");

        when(comercioRepository.existsByEmail("contacto@pizzeria.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servicioDeComercios.registrar(dto));
        verify(comercioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener un comercio por ID exitosamente")
    void testObtenerPorId_Exito() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercioMock));

        ComercioResponseDTO resultado = servicioDeComercios.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pizzeria Test", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si el comercio no existe por ID")
    void testObtenerPorId_NoEncontrado() {
        when(comercioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> servicioDeComercios.obtenerPorId(99L));
    }

    @Test
    @DisplayName("Debe obtener un comercio por email exitosamente")
    void testObtenerPorEmail_Exito() {
        when(comercioRepository.findByEmail("contacto@pizzeria.com"))
                .thenReturn(Optional.of(comercioMock));

        ComercioResponseDTO resultado =
                servicioDeComercios.obtenerPorEmail("contacto@pizzeria.com");

        assertNotNull(resultado);
        assertEquals("contacto@pizzeria.com", resultado.getEmail());
    }

    @Test
    @DisplayName("Debe listar todos los comercios")
    void testListar() {
        when(comercioRepository.findAll()).thenReturn(List.of(comercioMock));

        List<ComercioResponseDTO> lista = servicioDeComercios.listar();

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Debe eliminar un comercio existente")
    void testEliminar_Exito() {
        when(comercioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(comercioRepository).deleteById(1L);

        assertDoesNotThrow(() -> servicioDeComercios.eliminar(1L));
        verify(comercioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException al eliminar comercio inexistente")
    void testEliminar_NoEncontrado() {
        when(comercioRepository.existsById(99L)).thenReturn(false);

        assertThrows(NoSuchElementException.class,
                () -> servicioDeComercios.eliminar(99L));
        verify(comercioRepository, never()).deleteById(anyLong());
    }
}