package com.example.DA2Back.cotizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.cotizacion.dto.CotizacionDTO;
import com.example.DA2Back.cotizacion.negocio.CalculadoraTarifa;
import com.example.DA2Back.cotizacion.negocio.ServicioDeCotizacion;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.mapas.Coordenada;
import com.example.DA2Back.mapas.ProveedorMapas;
import com.example.DA2Back.mapas.RutaCalculada;
import com.example.DA2Back.mapas.ServicioMapas;

class ServicioDeCotizacionTest {

    private DepositoRepository depositos;
    private final CalculadoraTarifa tarifa = new CalculadoraTarifa(
            new BigDecimal("1500"), new BigDecimal("450"), new BigDecimal("120"),
            new BigDecimal("2000"), "ARS");

    @BeforeEach
    void setUp() {
        depositos = mock(DepositoRepository.class);
        Deposito d = new Deposito();
        d.setId(1L);
        d.setDireccion("Castro Barros 450, CABA");
        d.setComercio(Comercio.builder().id(7L).direccion("Av. Corrientes 1234, CABA").build());
        when(depositos.findById(1L)).thenReturn(Optional.of(d));
    }

    /** Ubica "Deposito" y "Destino" y devuelve 10 km por calles. */
    private ServicioMapas mapasQueResponden() {
        ProveedorMapas p = new ProveedorMapas() {
            @Override public String nombre() { return "FALSO"; }

            @Override public Optional<Coordenada> geocodificar(String direccion) {
                return Optional.of(direccion.contains("Castro")
                        ? new Coordenada(-34.60, -58.43) : new Coordenada(-34.60, -58.38));
            }

            @Override public Optional<RutaCalculada> calcularRuta(List<Coordenada> puntos) {
                return Optional.of(new RutaCalculada(10.0, 30.0,
                        List.of(new RutaCalculada.Tramo(10.0, 30.0)), puntos, "FALSO", false));
            }
        };
        return new ServicioMapas(Optional.of(p), 1.3, 25);
    }

    @Test
    void cotizaConDistanciaRealYCantidadDeObjetos() {
        var s = new ServicioDeCotizacion(depositos, mapasQueResponden(), tarifa, 5, 25);
        CotizacionDTO c = s.cotizar(1L, null, null, 4);

        assertEquals(10.0, c.distanciaKm(), 1e-9);
        assertEquals(30, c.duracionEstimadaMin());
        assertEquals(4, c.cantidadObjetos());
        // 1500 + 10*450 + 4*120 = 6480
        assertEquals(new BigDecimal("6480.00"), c.costoTotal());
        assertFalse(c.estimada());
        assertEquals("FALSO", c.metodo());
        // Origen = deposito, destino = comercio cuando no se informan direcciones
        assertEquals("Castro Barros 450, CABA", c.origen().direccion());
        assertEquals("Av. Corrientes 1234, CABA", c.destino().direccion());
    }

    @Test
    void respetaLasDireccionesInformadas() {
        var s = new ServicioDeCotizacion(depositos, mapasQueResponden(), tarifa, 5, 25);
        CotizacionDTO c = s.cotizar(1L, " Origen 1 ", "Destino 2", 1);
        assertEquals("Origen 1", c.origen().direccion());
        assertEquals("Destino 2", c.destino().direccion());
    }

    @Test
    void siNoSePuedeUbicarUsaLaDistanciaPorDefectoYLoMarcaComoEstimada() {
        // Sin proveedor: no hay geocodificacion
        var s = new ServicioDeCotizacion(depositos, new ServicioMapas(Optional.empty(), 1.3, 25), tarifa, 5, 25);
        CotizacionDTO c = s.cotizar(1L, null, null, 2);

        assertTrue(c.estimada());
        assertEquals("DISTANCIA_POR_DEFECTO", c.metodo());
        assertEquals(5.0, c.distanciaKm(), 1e-9);
        assertEquals(new BigDecimal("3990.00"), c.costoTotal()); // 1500 + 5*450 + 2*120
        assertEquals(null, c.origen().lat());
    }

    @Test
    void depositoInexistenteLanzaNoSuchElement() {
        var s = new ServicioDeCotizacion(depositos, mapasQueResponden(), tarifa, 5, 25);
        assertThrows(NoSuchElementException.class, () -> s.cotizar(99L, null, null, 1));
    }
}
