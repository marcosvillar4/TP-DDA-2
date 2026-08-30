package com.example.DA2Back.service;

import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.comercio.negocio.ServicioDeComerciosImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .nombre("Pizzeria Los Hijos de Puta")
                .direccion("Av. Mayo 500")
                .telefono("1122334455")
                .email("contacto@pizzeria.com")
                .build();
    }

    @Test
    @DisplayName("Debe crear un comercio exitosamente")
    void testCrearComercio_Exito() {
        ComercioCreateDTO dto = new ComercioCreateDTO();
        dto.setNombre("Pizzeria Los Hijos de Puta");
        dto.setDireccion("Av. Mayo 500");
        dto.setTelefono("1122334455");
        dto.setEmail("contacto@pizzeria.com");

        when(comercioRepository.existsByEmail("contacto@pizzeria.com")).thenReturn(false);
        when(comercioRepository.save(any(Comercio.class))).thenReturn(comercioMock);

        ComercioResponseDTO resultado = servicioDeComercios.crearComercio(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pizzeria Los Hijos de Puta", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe obtener un comercio por ID")
    void testObtenerPorId_Exito() {
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(comercioMock));

        ComercioResponseDTO resultado = servicioDeComercios.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pizzeria Los Hijos de Puta", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe listar todos los comercios")
    void testListarTodos() {
        when(comercioRepository.findAll()).thenReturn(List.of(comercioMock));

        List<ComercioResponseDTO> lista = servicioDeComercios.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }
}