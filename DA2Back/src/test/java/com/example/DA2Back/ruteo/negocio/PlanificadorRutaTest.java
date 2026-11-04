package com.example.DA2Back.ruteo.negocio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.example.DA2Back.mapas.Coordenada;

class PlanificadorRutaTest {

    /** Punto sobre el ecuador: x grados de longitud ~ x * 111 km, asi las distancias son faciles. */
    private static Coordenada x(double grados) {
        return new Coordenada(0, grados);
    }

    @Test
    void sinParadasDevuelveVacio() {
        assertEquals(0, PlanificadorRuta.ordenar(null, new Coordenada[0], new int[0]).length);
    }

    @Test
    void visitaPrimeroLoMasCercano() {
        Coordenada[] p = {x(1), x(3), x(2)};
        int[] orden = PlanificadorRuta.ordenar(x(0), p, new int[] {-1, -1, -1});
        assertArrayEquals(new int[] {0, 2, 1}, orden);
    }

    @Test
    void laEntregaSiempreVaDespuesDelRetiro() {
        // El destino (idx 1) esta mas cerca del repartidor que el deposito (idx 0)...
        Coordenada[] p = {x(10), x(1)};
        int[] orden = PlanificadorRuta.ordenar(x(0), p, new int[] {-1, 0});
        // ...pero igual hay que pasar por el deposito primero.
        assertArrayEquals(new int[] {0, 1}, orden);
    }

    @Test
    void combinaPedidosParaRecorrerMenosDistancia() {
        // 0=RetiroA(1) 1=EntregaA(10) 2=RetiroB(2) 3=EntregaB(3); desde x=0
        Coordenada[] p = {x(1), x(10), x(2), x(3)};
        int[] pre = {-1, 0, -1, 2};
        int[] orden = PlanificadorRuta.ordenar(x(0), p, pre);
        // Optimo: RA, RB, EB, EA = 10 grados; hacer un pedido por vez serian 19.
        assertArrayEquals(new int[] {0, 2, 3, 1}, orden);
    }

    @Test
    void sinPuntoDeInicioLaPrimeraParadaEsLibre() {
        Coordenada[] p = {x(5), x(1), x(2)};
        int[] orden = PlanificadorRuta.ordenar(null, p, new int[] {-1, -1, -1});
        // Empezar en x=1 (o x=5) es equivalente a recorrer 1->2->5: costo 4 grados.
        double costo = costo(p, orden);
        assertEquals(4 * 111.19, costo, 0.5);
    }

    @Test
    void prerrequisitosConCicloSonInvalidos() {
        Coordenada[] p = {x(1), x(2)};
        assertThrows(IllegalArgumentException.class,
                () -> PlanificadorRuta.ordenar(null, p, new int[] {1, 0}));
    }

    @Test
    void conMuchasParadasUsaHeuristicaPeroRespetaPrecedencias() {
        Random r = new Random(42);
        int pedidos = 8; // 16 paradas > LIMITE_EXACTO
        Coordenada[] p = new Coordenada[pedidos * 2];
        int[] pre = new int[pedidos * 2];
        for (int i = 0; i < pedidos; i++) {
            p[2 * i] = new Coordenada(-34.6 + r.nextDouble() * 0.1, -58.45 + r.nextDouble() * 0.1);
            p[2 * i + 1] = new Coordenada(-34.6 + r.nextDouble() * 0.1, -58.45 + r.nextDouble() * 0.1);
            pre[2 * i] = -1;
            pre[2 * i + 1] = 2 * i;
        }
        Coordenada inicio = new Coordenada(-34.58, -58.40);
        int[] orden = PlanificadorRuta.ordenar(inicio, p, pre);

        // Es una permutacion completa
        int[] copia = orden.clone();
        Arrays.sort(copia);
        for (int i = 0; i < copia.length; i++) assertEquals(i, copia[i]);

        // Cada retiro aparece antes que su entrega
        int[] pos = new int[orden.length];
        for (int i = 0; i < orden.length; i++) pos[orden[i]] = i;
        for (int i = 0; i < pedidos; i++) assertTrue(pos[2 * i] < pos[2 * i + 1]);

        // Y no es peor que atender los pedidos en el orden en que llegaron
        int[] ingreso = new int[p.length];
        for (int i = 0; i < ingreso.length; i++) ingreso[i] = i;
        assertTrue(costoDesde(inicio, p, orden) <= costoDesde(inicio, p, ingreso) + 1e-9);
    }

    private static double costo(Coordenada[] p, int[] orden) {
        double t = 0;
        for (int i = 1; i < orden.length; i++) t += Coordenada.distanciaKm(p[orden[i - 1]], p[orden[i]]);
        return t;
    }

    private static double costoDesde(Coordenada inicio, Coordenada[] p, int[] orden) {
        return Coordenada.distanciaKm(inicio, p[orden[0]]) + costo(p, orden);
    }
}
