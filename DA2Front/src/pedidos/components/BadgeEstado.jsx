import '../styles/BadgeEstado.css';

const config = {
  'EN_CAMINO': 'badge-violet',
  'ENTREGADO': 'badge-green',
  'PENDIENTE_COTIZACION': 'badge-amber',
  'ASIGNADO': 'badge-blue',
  'CANCELADO': 'badge-red'
};

const labels = {
  'EN_CAMINO': 'En tránsito',
  'ENTREGADO': 'Entregado',
  'PENDIENTE_COTIZACION': 'Pendiente',
  'ASIGNADO': 'Asignado',
  'CANCELADO': 'Cancelado'
};

export default function BadgeEstado({ estado }) {
  const badgeClass = config[estado] || 'badge-default';
  const label = labels[estado] || estado;
  
  return (
    <span className={`badge-estado ${badgeClass}`}>
      {label}
    </span>
  );
}
