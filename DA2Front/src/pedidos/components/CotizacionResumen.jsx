import { Calculator, Clock, Package, Route } from "lucide-react";
import { formatMoneda } from "../utils/formatMoneda";
import "../styles/CotizacionResumen.css";

const METODOS = {
  OSRM: "Distancia por calles (OpenStreetMap)",
  GOOGLE_ROUTES: "Distancia por calles (Google Maps)",
  ESTIMACION_LINEAL: "Distancia estimada en línea recta",
  DISTANCIA_POR_DEFECTO: "Distancia típica (no se pudo ubicar una dirección)",
};

/**
 * Muestra una cotización. `cotizacion` puede ser la respuesta de /api/pedidos/cotizar
 * (con desglose) o un objeto armado desde un pedido ya creado (sin desglose).
 */
export default function CotizacionResumen({ cotizacion, cargando = false, error = null }) {
  if (cargando && !cotizacion) {
    return <div className="cotizacion cotizacion-estado">Calculando cotización…</div>;
  }
  if (error && !cotizacion) {
    return <div className="cotizacion cotizacion-estado error">{error}</div>;
  }
  if (!cotizacion) {
    return (
      <div className="cotizacion cotizacion-estado">
        Completá el depósito para ver la cotización del envío.
      </div>
    );
  }

  const c = cotizacion;
  const moneda = c.moneda || "ARS";
  const hayDesglose = c.costoBase != null;

  return (
    <div className={`cotizacion ${cargando ? "actualizando" : ""}`}>
      <div className="cotizacion-total">
        <span className="cotizacion-total-label">
          <Calculator size={16} /> Costo del envío
        </span>
        <strong>{formatMoneda(c.costoTotal, moneda)}</strong>
      </div>

      <div className="cotizacion-datos">
        <span><Route size={14} /> {c.distanciaKm != null ? `${Number(c.distanciaKm).toFixed(2)} km` : "—"}</span>
        <span><Clock size={14} /> {c.duracionEstimadaMin != null ? `~${c.duracionEstimadaMin} min` : "—"}</span>
        <span><Package size={14} /> {c.cantidadObjetos ?? 0} objeto{c.cantidadObjetos === 1 ? "" : "s"}</span>
      </div>

      {hayDesglose && (
        <dl className="cotizacion-desglose">
          <div><dt>Tarifa base</dt><dd>{formatMoneda(c.costoBase, moneda)}</dd></div>
          <div><dt>Por distancia</dt><dd>{formatMoneda(c.costoPorDistancia, moneda)}</dd></div>
          <div><dt>Por cantidad de objetos</dt><dd>{formatMoneda(c.costoPorObjetos, moneda)}</dd></div>
          {c.tarifaMinimaAplicada && (
            <div className="nota"><dt>Se aplicó la tarifa mínima</dt><dd /></div>
          )}
        </dl>
      )}

      {c.estimada && (
        <p className="cotizacion-aviso">
          Importe aproximado: {METODOS[c.metodo] || "la distancia es una estimación"}.
        </p>
      )}
      {error && <p className="cotizacion-aviso error">{error}</p>}
    </div>
  );
}
