import '../styles/BadgeEstado.css';

const config = {
  'CREADO': 'badge-amber',
  'LISTO_PARA_RETIRAR': 'badge-orange',
  'ASIGNADO': 'badge-blue',
  'RETIRADO': 'badge-violet',
  'EN_CAMINO': 'badge-violet',
  'ENTREGADO': 'badge-green',
  'CANCELADO': 'badge-red'
};

const labels = {
  'CREADO': 'Creado',
  'LISTO_PARA_RETIRAR': 'Listo para Retirar',
  'ASIGNADO': 'Asignado',
  'RETIRADO': 'Retirado',
  'EN_CAMINO': 'En Camino',
  'ENTREGADO': 'Entregado',
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
