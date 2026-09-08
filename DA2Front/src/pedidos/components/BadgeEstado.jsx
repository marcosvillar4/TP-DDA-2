import '../styles/BadgeEstado.css';

const config = {
  'En tránsito': 'badge-violet',
  'Entregado': 'badge-green',
  'Pendiente': 'badge-amber',
  'Preparando': 'badge-blue',
  'Cancelado': 'badge-red'
};

export default function BadgeEstado({ estado }) {
  const badgeClass = config[estado] || 'badge-default';
  
  return (
    <span className={`badge-estado ${badgeClass}`}>
      {estado}
    </span>
  );
}
