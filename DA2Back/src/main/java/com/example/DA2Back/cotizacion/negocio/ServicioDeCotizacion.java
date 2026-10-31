package com.example.DA2Back.cotizacion.negocio;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.DA2Back.cotizacion.dto.CotizacionDTO;
import com.example.DA2Back.cotizacion.dto.PuntoDTO;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.mapas.Coordenada;
import com.example.DA2Back.mapas.RutaCalculada;
import com.example.DA2Back.mapas.ServicioMapas;

/**
 * Cotiza un envio: ubica origen y destino, calcula la distancia por calles
 * y aplica {@link CalculadoraTarifa}. No valida permisos ni stock: eso lo hace
 * ServicioDePedidos antes de invocarlo.
 */
@Service
public class ServicioDeCotizacion {

    private final DepositoRepository depositoRepository;
    private final ServicioMapas mapas;
    private final CalculadoraTarifa tarifa;
    private final double distanciaPorDefectoKm;
    private final double velocidadKmh;

    public ServicioDeCotizacion(DepositoRepository depositoRepository,
                                ServicioMapas mapas,
                                CalculadoraTarifa tarifa,
                                @Value("${app.cotizacion.distancia-defecto-km:5}") double distanciaPorDefectoKm,
                                @Value("${app.mapas.velocidad-promedio-kmh:25}") double velocidadKmh) {
        this.depositoRepository = depositoRepository;
        this.mapas = mapas;
        this.tarifa = tarifa;
        this.distanciaPorDefectoKm = distanciaPorDefectoKm;
        this.velocidadKmh = velocidadKmh;
    }

    /**
     * @param direccionOrigen  si es vacia se usa la del deposito
     * @param direccionDestino si es vacia se usa la del comercio del deposito
     */
    public CotizacionDTO cotizar(Long depositoId, String direccionOrigen, String direccionDestino,
                                 int cantidadObjetos) {
        Deposito deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new NoSuchElementException("Deposito no encontrado"));

        String origenTxt = noVacia(direccionOrigen) ? direccionOrigen.trim() : deposito.getDireccion();
        String destinoTxt = noVacia(direccionDestino) ? direccionDestino.trim()
                : (deposito.getComercio() != null ? deposito.getComercio().getDireccion() : null);
        if (!noVacia(destinoTxt)) {
            throw new IllegalArgumentException("La direccion de destino es obligatoria");
        }

        Optional<Coordenada> origen = mapas.geocodificar(origenTxt);
        Optional<Coordenada> destino = mapas.geocodificar(destinoTxt);

        double km;
        double min;
        String metodo;
        boolean estimada;
        if (origen.isPresent() && destino.isPresent()) {
            RutaCalculada ruta = mapas.calcularRuta(List.of(origen.get(), destino.get()));
            km = ruta.distanciaKm();
            min = ruta.duracionMin();
            metodo = ruta.metodo();
            estimada = ruta.estimada();
        } else {
            // No se pudo ubicar alguna direccion: cotizamos con una distancia tipica configurable.
            km = distanciaPorDefectoKm;
            min = km / velocidadKmh * 60.0;
            metodo = "DISTANCIA_POR_DEFECTO";
            estimada = true;
        }
        km = Math.round(km * 100.0) / 100.0;

        CalculadoraTarifa.Desglose d = tarifa.calcular(km, cantidadObjetos);
        return new CotizacionDTO(km, (int) Math.max(1, Math.round(min)), cantidadObjetos,
                d.base(), d.porDistancia(), d.porObjetos(), d.total(), d.moneda(),
                d.minimaAplicada(), estimada, metodo,
                new PuntoDTO(origenTxt, origen.map(Coordenada::lat).orElse(null),
                        origen.map(Coordenada::lng).orElse(null)),
                new PuntoDTO(destinoTxt, destino.map(Coordenada::lat).orElse(null),
                        destino.map(Coordenada::lng).orElse(null)));
    }

    private static boolean noVacia(String s) {
        return s != null && !s.isBlank();
    }
}
