package com.example.DA2Back.ruteo.negocio;

import com.example.DA2Back.mapas.Coordenada;

/**
 * Ordena las paradas de un repartidor para minimizar el recorrido total.
 *
 * Es un problema tipo "viajante con precedencias": una ENTREGA solo puede
 * hacerse despues del RETIRO del mismo pedido. Las distancias usadas para
 * ordenar son en linea recta (Haversine); luego el ServicioMapas calcula la
 * distancia real por calles sobre el orden ya elegido, lo que evita pedir una
 * matriz de distancias completa a un servicio externo.
 *
 *  - Hasta {@link #LIMITE_EXACTO} paradas: busqueda exhaustiva con poda (optimo).
 *  - Mas paradas: vecino mas cercano + mejora por reubicacion (heuristica).
 */
public final class PlanificadorRuta {

    static final int LIMITE_EXACTO = 9;

    private PlanificadorRuta() { }

    /**
     * @param inicio        punto de partida del repartidor (puede ser null: entonces la
     *                      primera parada es libre y no suma distancia)
     * @param puntos        ubicacion de cada parada
     * @param prerrequisito prerrequisito[i] = indice de la parada que debe hacerse antes
     *                      que i, o -1 si no tiene
     * @return indices de las paradas en el orden de visita
     */
    public static int[] ordenar(Coordenada inicio, Coordenada[] puntos, int[] prerrequisito) {
        int n = puntos.length;
        if (prerrequisito.length != n) throw new IllegalArgumentException("Arreglos de distinto largo");
        if (n == 0) return new int[0];
        double[][] d = matriz(puntos);
        double[] desdeInicio = new double[n];
        for (int i = 0; i < n; i++) {
            desdeInicio[i] = inicio == null ? 0 : Coordenada.distanciaKm(inicio, puntos[i]);
        }
        return n <= LIMITE_EXACTO ? exacto(n, d, desdeInicio, prerrequisito)
                                  : heuristico(n, d, desdeInicio, prerrequisito);
    }

    // ---------------------------------------------------------------- exacto

    private static int[] exacto(int n, double[][] d, double[] desdeInicio, int[] pre) {
        Estado e = new Estado(n);
        e.mejorCosto = Double.MAX_VALUE;
        dfs(e, d, desdeInicio, pre, 0, -1, 0.0, 0);
        if (e.mejor == null) throw new IllegalArgumentException("Prerrequisitos inconsistentes (ciclo)");
        return e.mejor;
    }

    private static final class Estado {
        final int[] actual;
        int[] mejor;
        double mejorCosto;

        Estado(int n) {
            actual = new int[n];
        }
    }

    private static void dfs(Estado e, double[][] d, double[] desdeInicio, int[] pre,
                            int visitadas, int ultima, double costo, int nivel) {
        int n = e.actual.length;
        if (costo >= e.mejorCosto) return; // poda
        if (nivel == n) {
            e.mejorCosto = costo;
            e.mejor = e.actual.clone();
            return;
        }
        for (int i = 0; i < n; i++) {
            if ((visitadas & (1 << i)) != 0) continue;
            if (pre[i] >= 0 && (visitadas & (1 << pre[i])) == 0) continue;
            double tramo = ultima < 0 ? desdeInicio[i] : d[ultima][i];
            e.actual[nivel] = i;
            dfs(e, d, desdeInicio, pre, visitadas | (1 << i), i, costo + tramo, nivel + 1);
        }
    }

    // ------------------------------------------------------------- heuristico

    private static int[] heuristico(int n, double[][] d, double[] desdeInicio, int[] pre) {
        int[] orden = new int[n];
        boolean[] usada = new boolean[n];
        int ultima = -1;
        for (int paso = 0; paso < n; paso++) {
            int elegida = -1;
            double mejor = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (usada[i] || (pre[i] >= 0 && !usada[pre[i]])) continue;
                double c = ultima < 0 ? desdeInicio[i] : d[ultima][i];
                if (c < mejor) {
                    mejor = c;
                    elegida = i;
                }
            }
            if (elegida < 0) throw new IllegalArgumentException("Prerrequisitos inconsistentes (ciclo)");
            orden[paso] = elegida;
            usada[elegida] = true;
            ultima = elegida;
        }
        // Mejora: mover una parada a otra posicion mientras reduzca el costo y respete precedencias.
        boolean mejora = true;
        double costo = costo(orden, d, desdeInicio);
        while (mejora) {
            mejora = false;
            for (int from = 0; from < n && !mejora; from++) {
                for (int to = 0; to < n && !mejora; to++) {
                    if (from == to) continue;
                    int[] candidato = mover(orden, from, to);
                    if (!valido(candidato, pre)) continue;
                    double c = costo(candidato, d, desdeInicio);
                    if (c + 1e-9 < costo) {
                        orden = candidato;
                        costo = c;
                        mejora = true;
                    }
                }
            }
        }
        return orden;
    }

    private static int[] mover(int[] orden, int from, int to) {
        int[] r = new int[orden.length];
        int valor = orden[from];
        int k = 0;
        for (int i = 0; i < orden.length; i++) {
            if (i == from) continue;
            if (k == to) r[k++] = valor;
            r[k++] = orden[i];
        }
        if (k < orden.length) r[k] = valor;
        return r;
    }

    private static boolean valido(int[] orden, int[] pre) {
        int[] posicion = new int[orden.length];
        for (int i = 0; i < orden.length; i++) posicion[orden[i]] = i;
        for (int i = 0; i < orden.length; i++) {
            if (pre[i] >= 0 && posicion[pre[i]] > posicion[i]) return false;
        }
        return true;
    }

    // ---------------------------------------------------------------- comunes

    /** Costo total (km en linea recta) de recorrer las paradas en ese orden. */
    static double costo(int[] orden, double[][] d, double[] desdeInicio) {
        double total = 0;
        for (int i = 0; i < orden.length; i++) {
            total += i == 0 ? desdeInicio[orden[0]] : d[orden[i - 1]][orden[i]];
        }
        return total;
    }

    private static double[][] matriz(Coordenada[] p) {
        double[][] d = new double[p.length][p.length];
        for (int i = 0; i < p.length; i++) {
            for (int j = i + 1; j < p.length; j++) {
                d[i][j] = Coordenada.distanciaKm(p[i], p[j]);
                d[j][i] = d[i][j];
            }
        }
        return d;
    }
}
