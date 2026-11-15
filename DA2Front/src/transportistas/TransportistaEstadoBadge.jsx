import "./styles/TransportistaEstadoBadge.css";

const labels = { DISPONIBLE: "Disponible", EN_SERVICIO: "En servicio", NO_DISPONIBLE: "No disponible" };

export default function TransportistaEstadoBadge({ estado, activo }) {
  if (!activo) return <span className="transportista-badge transportista-badge-inactivo">Inactivo</span>;
  return <span className={`transportista-badge transportista-badge-${estado?.toLowerCase()}`}>{labels[estado] || estado || "Sin estado"}</span>;
}
