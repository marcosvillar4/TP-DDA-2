import { useCallback, useEffect, useRef, useState } from "react";
import { LocateFixed, MapPin, PackageCheck, RefreshCw, Route } from "lucide-react";
import { getMiRuta } from "../../../api/repartidorPerfilApi";
import MapaRuta from "../../mapa/MapaRuta";
import "../styles/RutaRepartidor.css";

/**
 * Recorrido pendiente del repartidor: paradas ordenadas (retiro -> entrega),
 * distancia/tiempo por tramo y mapa. `refreshKey` cambia cuando cambia el pedido
 * actual o su estado, para recalcular la ruta.
 */
export default function RutaRepartidor({ refreshKey }) {
  const [ruta, setRuta] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);
  const [ubicacion, setUbicacion] = useState(null);
  const [buscandoUbicacion, setBuscandoUbicacion] = useState(false);
  const [errorUbicacion, setErrorUbicacion] = useState(null);
  const montadoRef = useRef(false);
  const pedidoReqRef = useRef(0);

  const cargar = useCallback(async (coords) => {
    const req = ++pedidoReqRef.current;
    setCargando(true);
    setError(null);
    try {
      const data = await getMiRuta(coords?.lat, coords?.lng);
      if (!montadoRef.current || req !== pedidoReqRef.current) return;
      setRuta(data);
    } catch (err) {
      if (!montadoRef.current || req !== pedidoReqRef.current) return;
      setError(err?.message || "No pudimos calcular tu recorrido.");
    } finally {
      if (montadoRef.current && req === pedidoReqRef.current) setCargando(false);
    }
  }, []);

  useEffect(() => {
    montadoRef.current = true;
    const t = setTimeout(() => cargar(ubicacion), 0);
    return () => {
      montadoRef.current = false;
      clearTimeout(t);
    };
    // La ubicacion se aplica solo cuando el repartidor la pide (usarMiUbicacion).
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cargar, refreshKey]);

  function usarMiUbicacion() {
    if (!navigator.geolocation) {
      setErrorUbicacion("Tu navegador no permite obtener la ubicación.");
      return;
    }
    setBuscandoUbicacion(true);
    setErrorUbicacion(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const coords = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        setBuscandoUbicacion(false);
        setUbicacion(coords);
        cargar(coords);
      },
      () => {
        setBuscandoUbicacion(false);
        setErrorUbicacion("No pudimos obtener tu ubicación. Revisá los permisos del navegador.");
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  const paradas = ruta?.paradas ?? [];

  return (
    <section className="mis-pedidos-card ruta-repartidor">
      <div className="mis-pedidos-card-header">
        <div>
          <h2>Mi recorrido</h2>
          <p>Orden sugerido de paradas para recorrer la menor distancia.</p>
        </div>
        <div className="ruta-acciones">
          <button
            className="mis-pedidos-btn secondary"
            type="button"
            onClick={usarMiUbicacion}
            disabled={buscandoUbicacion || cargando}
            title="Calcula el recorrido desde donde estás ahora"
          >
            <LocateFixed size={16} />
            {buscandoUbicacion ? "Ubicando..." : ubicacion ? "Actualizar ubicación" : "Usar mi ubicación"}
          </button>
          <button
            className="mis-pedidos-btn secondary"
            type="button"
            onClick={() => cargar(ubicacion)}
            disabled={cargando}
          >
            <RefreshCw size={16} className={cargando ? "mis-pedidos-spin" : ""} />
            Recalcular
          </button>
        </div>
      </div>

      {errorUbicacion && <p className="ruta-aviso error">{errorUbicacion}</p>}
      {error && <p className="ruta-aviso error" role="alert">{error}</p>}

      {!error && ruta && paradas.length === 0 && (
        <div className="mis-pedidos-empty compact">
          <PackageCheck size={24} />
          <strong>No tenés paradas pendientes</strong>
          <span>Cuando tomes un pedido, acá vas a ver el recorrido.</span>
        </div>
      )}

      {paradas.length > 0 && (
        <>
          <div className="ruta-resumen">
            <span><Route size={14} /> {ruta.distanciaTotalKm.toFixed(2)} km</span>
            <span>~{ruta.duracionTotalMin} min</span>
            <span>{paradas.length} parada{paradas.length === 1 ? "" : "s"}</span>
          </div>

          <ol className="ruta-paradas">
            {paradas.map((p) => (
              <li key={`${p.pedidoId}-${p.tipo}`}>
                <span className={`ruta-numero ${p.tipo.toLowerCase()}`}>{p.orden}</span>
                <div className="ruta-parada-texto">
                  <strong>
                    {p.tipo === "RETIRO" ? "Retirar" : "Entregar"} · pedido #{p.pedidoId}
                  </strong>
                  <span><MapPin size={12} /> {p.direccion}</span>
                </div>
                <span className="ruta-parada-tramo">
                  {p.distanciaDesdeAnteriorKm > 0
                    ? `${p.distanciaDesdeAnteriorKm.toFixed(2)} km · ${p.duracionDesdeAnteriorMin} min`
                    : "—"}
                </span>
              </li>
            ))}
          </ol>

          <MapaRuta ruta={ruta} />

          {ruta.estimada && (
            <p className="ruta-aviso">
              Distancias aproximadas: no se pudo consultar el servicio de mapas, se estimaron en línea recta.
            </p>
          )}
        </>
      )}

      {ruta?.advertencias?.length > 0 && (
        <ul className="ruta-advertencias">
          {ruta.advertencias.map((a) => <li key={a}>{a}</li>)}
        </ul>
      )}

      {cargando && !ruta && <div className="mis-pedidos-table-empty">Calculando recorrido...</div>}
    </section>
  );
}
