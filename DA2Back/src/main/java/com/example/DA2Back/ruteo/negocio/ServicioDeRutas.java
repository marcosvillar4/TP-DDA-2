package com.example.DA2Back.ruteo.negocio;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.cotizacion.dto.PuntoDTO;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.mapas.Coordenada;
import com.example.DA2Back.mapas.RutaCalculada;
import com.example.DA2Back.mapas.ServicioMapas;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.negocio.AccesoPedidos;
import com.example.DA2Back.ruteo.dto.ParadaRutaDTO;
import com.example.DA2Back.ruteo.dto.RutaDTO;

import lombok.RequiredArgsConstructor;

/**
 * Sistema de ruteo: arma el recorrido que realiza el repartidor.
 *
 *  1. Por cada pedido activo agrega las paradas pendientes: RETIRO (si todavia no
 *     lo retiro) y ENTREGA. La entrega siempre va despues de su retiro.
 *  2. {@link PlanificadorRuta} elige el orden que minimiza el recorrido.
 *  3. {@link ServicioMapas} calcula distancia/tiempo por calles y la polilinea.
 */
@Service
@RequiredArgsConstructor
public class ServicioDeRutas {

    private static final List<EstadoPedido> ESTADOS_ACTIVOS =
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO);

    private final PedidosRepository pedidosRepository;
    private final DepositoRepository depositoRepository;
    private final ServicioMapas mapas;
    private final AccesoPedidos accesoPedidos;

    /** Parada candidata antes de ordenar. */
    private record Candidata(String tipo, Pedido pedido, String direccion, Coordenada coordenada,
                             boolean dependeDeRetiro) { }

    /**
     * Recorrido pendiente del repartidor con los pedidos que tiene activos.
     *
     * @param lat/lng ubicacion actual del repartidor (opcional, p. ej. del GPS del navegador)
     */
    @Transactional
    public RutaDTO rutaDeRepartidor(Long repartidorId, Double lat, Double lng) {
        List<Pedido> pedidos = pedidosRepository.findByRepartidorIdAndEstadoIn(repartidorId, ESTADOS_ACTIVOS);
        List<String> advertencias = new ArrayList<>();
        List<Candidata> candidatas = new ArrayList<>();
        for (Pedido p : pedidos) {
            boolean pendienteRetiro = p.getEstado() == EstadoPedido.ASIGNADO;
            agregarParadas(p, pendienteRetiro, candidatas, advertencias);
        }
        return armar(candidatas, coordenadaOpcional(lat, lng), advertencias);
    }

    /** Recorrido completo (retiro -> entrega) de un pedido, respetando el alcance del usuario. */
    @Transactional
    public RutaDTO rutaDePedido(Long pedidoId, Usuario usuario) {
        var alcance = accesoPedidos.resolver(usuario, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO);
        Pedido pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException("Pedido con id=" + pedidoId + " no encontrado"));
        alcance.validarPedido(pedido);
        List<String> advertencias = new ArrayList<>();
        List<Candidata> candidatas = new ArrayList<>();
        agregarParadas(pedido, true, candidatas, advertencias);
        return armar(candidatas, null, advertencias);
    }

    // ------------------------------------------------------------------ internos

    private void agregarParadas(Pedido p, boolean incluirRetiro, List<Candidata> out, List<String> advertencias) {
        asegurarCoordenadas(p);
        boolean retiroIncluido = false;
        if (incluirRetiro) {
            String dirOrigen = direccionOrigen(p);
            if (p.getOrigenLat() != null && p.getOrigenLng() != null) {
                out.add(new Candidata("RETIRO", p, dirOrigen,
                        new Coordenada(p.getOrigenLat(), p.getOrigenLng()), false));
                retiroIncluido = true;
            } else {
                advertencias.add("Pedido #" + p.getId() + ": no se pudo ubicar la direccion de retiro");
            }
        }
        if (p.getDestinoLat() != null && p.getDestinoLng() != null) {
            out.add(new Candidata("ENTREGA", p, p.getDireccionDestino(),
                    new Coordenada(p.getDestinoLat(), p.getDestinoLng()), retiroIncluido));
        } else {
            advertencias.add("Pedido #" + p.getId() + ": no se pudo ubicar la direccion de entrega");
        }
    }

    /** Pedidos anteriores a la cotizacion no tienen coordenadas: se geocodifican una vez y se guardan. */
    private void asegurarCoordenadas(Pedido p) {
        boolean cambio = false;
        if (p.getOrigenLat() == null || p.getOrigenLng() == null) {
            Optional<Coordenada> c = mapas.geocodificar(direccionOrigen(p));
            if (c.isPresent()) {
                p.setOrigenLat(c.get().lat());
                p.setOrigenLng(c.get().lng());
                cambio = true;
            }
        }
        if (p.getDestinoLat() == null || p.getDestinoLng() == null) {
            Optional<Coordenada> c = mapas.geocodificar(p.getDireccionDestino());
            if (c.isPresent()) {
                p.setDestinoLat(c.get().lat());
                p.setDestinoLng(c.get().lng());
                cambio = true;
            }
        }
        if (cambio) pedidosRepository.save(p);
    }

    private String direccionOrigen(Pedido p) {
        if (p.getDireccionOrigen() != null && !p.getDireccionOrigen().isBlank()) return p.getDireccionOrigen();
        return depositoRepository.findById(p.getDepositoId()).map(d -> d.getDireccion()).orElse(null);
    }

    private RutaDTO armar(List<Candidata> candidatas, Coordenada inicio, List<String> advertencias) {
        PuntoDTO partida = inicio == null ? null : new PuntoDTO("Ubicacion actual", inicio.lat(), inicio.lng());
        if (candidatas.isEmpty()) {
            return new RutaDTO(partida, List.of(), 0, 0, List.of(), mapas.nombreProveedor(), false, advertencias);
        }

        // 1) Orden optimo respetando "retiro antes que entrega".
        Coordenada[] puntos = new Coordenada[candidatas.size()];
        int[] prerrequisito = new int[candidatas.size()];
        for (int i = 0; i < candidatas.size(); i++) {
            Candidata c = candidatas.get(i);
            puntos[i] = c.coordenada();
            prerrequisito[i] = -1;
            if (c.dependeDeRetiro()) {
                for (int j = 0; j < candidatas.size(); j++) {
                    Candidata r = candidatas.get(j);
                    if (r.tipo().equals("RETIRO") && r.pedido().getId().equals(c.pedido().getId())) {
                        prerrequisito[i] = j;
                    }
                }
            }
        }
        int[] orden = PlanificadorRuta.ordenar(inicio, puntos, prerrequisito);

        // 2) Distancias y tiempos reales sobre ese orden.
        List<Coordenada> recorrido = new ArrayList<>();
        if (inicio != null) recorrido.add(inicio);
        for (int idx : orden) recorrido.add(puntos[idx]);

        RutaCalculada calculada = recorrido.size() >= 2 ? mapas.calcularRuta(recorrido) : null;

        List<ParadaRutaDTO> paradas = new ArrayList<>();
        double acumulado = 0;
        for (int k = 0; k < orden.length; k++) {
            Candidata c = candidatas.get(orden[k]);
            double km = 0;
            double min = 0;
            if (calculada != null) {
                int tramo = inicio != null ? k : k - 1; // sin partida, la 1ra parada no tiene tramo previo
                if (tramo >= 0) {
                    km = calculada.tramos().get(tramo).distanciaKm();
                    min = calculada.tramos().get(tramo).duracionMin();
                }
            }
            acumulado += min;
            paradas.add(new ParadaRutaDTO(k + 1, c.tipo(), c.pedido().getId(), c.direccion(),
                    c.coordenada().lat(), c.coordenada().lng(),
                    redondear(km), (int) Math.round(min), (int) Math.round(acumulado)));
        }

        List<double[]> geometria = new ArrayList<>();
        List<Coordenada> linea = calculada != null && !calculada.geometria().isEmpty()
                ? calculada.geometria() : recorrido;
        for (Coordenada c : linea) geometria.add(new double[] {c.lat(), c.lng()});

        return new RutaDTO(partida, paradas,
                calculada == null ? 0 : redondear(calculada.distanciaKm()),
                calculada == null ? 0 : (int) Math.round(calculada.duracionMin()),
                geometria,
                calculada == null ? mapas.nombreProveedor() : calculada.metodo(),
                calculada != null && calculada.estimada(),
                advertencias);
    }

    private static Coordenada coordenadaOpcional(Double lat, Double lng) {
        if (lat == null || lng == null) return null;
        Coordenada c = new Coordenada(lat, lng);
        return c.esValida() ? c : null;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
