import "../styles/RepartidorEstadoBadge.css";

const labels = {
  DISPONIBLE: "Disponible",
  EN_ENTREGA: "En entrega",
  NO_DISPONIBLE: "No disponible",
};

export default function RepartidorEstadoBadge({ estado, activo = true }) {
  if (!activo) {
    return <span className="repartidor-badge repartidor-badge-inactivo">Inactivo</span>;
  }

  return (
    <span className={`repartidor-badge repartidor-badge-${estado?.toLowerCase()}`}>
      {labels[estado] || estado || "Sin estado"}
    </span>
  );
}
