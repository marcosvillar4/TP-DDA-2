package com.example.DA2Back.mapas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GoogleMapsProveedorMapasTest {

    @Test
    void decodificaLaPolilineaDeEjemploDeGoogle() {
        // Ejemplo de la documentacion de Google: "Encoded Polyline Algorithm Format"
        List<Coordenada> puntos = GoogleMapsProveedorMapas.decodificarPolilinea("_p~iF~ps|U_ulLnnqC_mqNvxq`@");
        assertEquals(3, puntos.size());
        assertEquals(38.5, puntos.get(0).lat(), 1e-5);
        assertEquals(-120.2, puntos.get(0).lng(), 1e-5);
        assertEquals(40.7, puntos.get(1).lat(), 1e-5);
        assertEquals(-120.95, puntos.get(1).lng(), 1e-5);
        assertEquals(43.252, puntos.get(2).lat(), 1e-5);
        assertEquals(-126.453, puntos.get(2).lng(), 1e-5);
    }

    @Test
    void polilineaNulaDaListaVacia() {
        assertTrue(GoogleMapsProveedorMapas.decodificarPolilinea(null).isEmpty());
    }

    @Test
    void sinApiKeyNoLlamaALaRedYDevuelveVacio() {
        GoogleMapsProveedorMapas g = new GoogleMapsProveedorMapas("", "ar", 1000);
        assertFalse(g.geocodificar("Av. Corrientes 1234").isPresent());
        assertFalse(g.calcularRuta(List.of(new Coordenada(0, 0), new Coordenada(1, 1))).isPresent());
    }
}
