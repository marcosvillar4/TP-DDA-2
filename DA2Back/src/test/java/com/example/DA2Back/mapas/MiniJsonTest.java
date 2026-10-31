package com.example.DA2Back.mapas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MiniJsonTest {

    @Test
    void parseaObjetosArraysYTipos() {
        Object raiz = MiniJson.parse(
                "{\"code\":\"Ok\",\"routes\":[{\"distance\":1234.5,\"legs\":[{\"duration\":60}]}],"
                + "\"ok\":true,\"nada\":null,\"txt\":\"a\\\"b\\u00e1\"}");
        Map<String, Object> m = MiniJson.obj(raiz);
        assertEquals("Ok", m.get("code"));
        assertEquals(Boolean.TRUE, m.get("ok"));
        assertNull(m.get("nada"));
        assertEquals("a\"b\u00e1", m.get("txt"));
        List<Object> rutas = MiniJson.arr(m.get("routes"));
        assertEquals(1234.5, MiniJson.num(MiniJson.obj(rutas.get(0)).get("distance")), 1e-9);
    }

    @Test
    void numeroPuedeVenirComoString() {
        // Nominatim devuelve lat/lon como texto.
        assertEquals(-34.6, MiniJson.num("-34.6"), 1e-9);
        assertNull(MiniJson.num("abc"));
    }

    @Test
    void parseaArrayVacioYNegativos() {
        List<Object> l = MiniJson.arr(MiniJson.parse(" [ ] "));
        assertTrue(l.isEmpty());
        assertEquals(-58.38, MiniJson.num(MiniJson.arr(MiniJson.parse("[-58.38]")).get(0)), 1e-9);
    }

    @Test
    void jsonInvalidoLanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> MiniJson.parse("{\"a\":"));
        assertThrows(RuntimeException.class, () -> MiniJson.parse("{\"a\":1} basura"));
    }
}
