import { labelEstadoComercio } from '../utils/comercioEstado';
import '../styles/BadgeComercio.css';

const CLASES = {
  VALIDADO: 'badge-comercio-activo',
  EN_EVALUACION: 'badge-comercio-pendiente',
};

export default function BadgeComercio({ estado }) {
  const clase = CLASES[estado] ?? 'badge-comercio-inactivo';

  return (
    <span className={`badge-comercio ${clase}`}>
      {labelEstadoComercio(estado)}
    </span>
  );
}
