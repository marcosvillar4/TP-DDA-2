import "../styles/ProductoEstadoBadge.css";

const estados = {
  ACTIVO: {
    label: "Activo",
    className: "producto-estado-activo",
  },
  INACTIVO: {
    label: "Inactivo",
    className: "producto-estado-inactivo",
  },
};

export function ProductoEstadoBadge({ estado }) {
  const config = estados[estado] ?? {
    label: estado ?? "Sin estado",
    className: "producto-estado-default",
  };

  return (
    <span className={`producto-estado-badge ${config.className}`}>
      {config.label}
    </span>
  );
}
