import '../styles/BadgeComercio.css';

export default function BadgeComercio({ estado }) {
  const isActivo = estado === 'Activo';
  
  return (
    <span className={`badge-comercio ${isActivo ? 'badge-comercio-activo' : 'badge-comercio-inactivo'}`}>
      {estado}
    </span>
  );
}
