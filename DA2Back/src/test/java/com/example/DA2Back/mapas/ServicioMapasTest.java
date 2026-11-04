package com.example.DA2Back.mapas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ServicioMapasTest {

    private static final Coordenada OBELISCO = new Coordenada(-34.6037, -58.3816);
    private static final Coordenada PALERMO = new Coordenada(-34.5875, -58.4300);

    /** Proveedor falso que cuenta las llamadas. */
    private static class Falso implements ProveedorMapas {
        int geocodificaciones = 0;
        boolean rutaDisponible = true;

        @Override public String nombre() { return "FALSO"; }

        @Override public Optional<Coordenada> geocodificar(String direccion) {
            geocodificaciones++;
            return direccion.contains("Nada") ? Optional.empty() : Optional.of(OBELISCO);
        }

        @Override public Optional<RutaCalculada> calcularRuta(List<Coordenada> puntos) {
            if (!rutaDisponible) return Optional.empty();
            return Optional.of(new RutaCalculada(7.0, 20.0,
                    List.of(new RutaCalculada.Tramo(7.0, 20.0)), puntos, "FALSO", false));
        }
    }

    @Test
    void haversineEntreDosPuntosConocidos() {
        // 1 grado de longitud sobre el ecuador ~ 111.19 km
        assertEquals(111.19, Coordenada.distanciaKm(new Coordenada(0, 0), new Coordenada(0, 1)), 0.05);
        assertEquals(0.0, Coordenada.distanciaKm(OBELISCO, OBELISCO), 1e-9);
        // Obelisco -> punto de Palermo usado en el test: ~4.78 km en linea recta
        // (1.80 km en latitud y 4.43 km en longitud a esa latitud).
        assertEquals(4.78, Coordenada.distanciaKm(OBELISCO, PALERMO), 0.05);
    }

    @Test
    void geocodificarUsaCacheYNoRepiteLlamadas() {
        Falso proveedor = new Falso();
        ServicioMapas s = new ServicioMapas(Optional.of(proveedor), 1.3, 25);
        assertTrue(s.geocodificar("Av. Corrientes 1234, CABA").isPresent());
        assertTrue(s.geocodificar("  av. corrientes   1234, caba ").isPresent());
        assertEquals(1, proveedor.geocodificaciones);
    }

    @Test
    void noCacheaLosFallos() {
        Falso proveedor = new Falso();
        ServicioMapas s = new ServicioMapas(Optional.of(proveedor), 1.3, 25);
        assertFalse(s.geocodificar("Nada 1").isPresent());
        assertFalse(s.geocodificar("Nada 1").isPresent());
        assertTrue(proveedor.geocodificaciones >= 2);
    }

    @Test
    void sinProveedorNoGeocodificaPeroSiEstimaRutas() {
        ServicioMapas s = new ServicioMapas(Optional.empty(), 1.3, 25);
        assertEquals("LOCAL", s.nombreProveedor());
        assertFalse(s.geocodificar("Cualquiera 123").isPresent());

        RutaCalculada r = s.calcularRuta(List.of(OBELISCO, PALERMO));
        assertTrue(r.estimada());
        assertEquals("ESTIMACION_LINEAL", r.metodo());
        double lineal = Coordenada.distanciaKm(OBELISCO, PALERMO);
        assertEquals(lineal * 1.3, r.distanciaKm(), 1e-9);
        assertEquals(r.distanciaKm() / 25 * 60, r.duracionMin(), 1e-9);
        assertEquals(1, r.tramos().size());
    }

    @Test
    void siElProveedorFallaSeEstimaLocalmente() {
        Falso proveedor = new Falso();
        proveedor.rutaDisponible = false;
        ServicioMapas s = new ServicioMapas(Optional.of(proveedor), 1.3, 25);
        assertTrue(s.calcularRuta(List.of(OBELISCO, PALERMO)).estimada());
    }

    @Test
    void usaLaRutaRealCuandoElProveedorResponde() {
        ServicioMapas s = new ServicioMapas(Optional.of(new Falso()), 1.3, 25);
        RutaCalculada r = s.calcularRuta(List.of(OBELISCO, PALERMO));
        assertFalse(r.estimada());
        assertEquals(7.0, r.distanciaKm(), 1e-9);
        assertEquals("FALSO", r.metodo());
    }

    @Test
    void rutaConMenosDeDosPuntosEsInvalida() {
        ServicioMapas s = new ServicioMapas(Optional.empty(), 1.3, 25);
        assertThrows(IllegalArgumentException.class, () -> s.calcularRuta(List.of(OBELISCO)));
    }

    @Test
    void normalizaEspaciosYCaba() {
        assertEquals("Av. Corrientes 1234, Buenos Aires",
                ServicioMapas.normalizar("  Av. Corrientes   1234, CABA "));
        assertEquals("Castro Barros 450, Buenos Aires", ServicioMapas.normalizar("Castro Barros 450, caba"));
    }
}
