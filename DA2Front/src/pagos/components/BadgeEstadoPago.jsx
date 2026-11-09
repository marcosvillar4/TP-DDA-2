import '../../pedidos/styles/BadgeEstado.css';

const CLASES = {
  PENDIENTE: 'badge-amber',
  PAGADO: 'badge-green',
  ANULADO: 'badge-default',
};

const ETIQUETAS_COMERCIO = {
  PENDIENTE: 'Pendiente de pago',
  PAGADO: 'Pagado',
  ANULADO: 'Anulado',
};

const ETIQUETAS_REPARTIDOR = {
  PENDIENTE: 'Pendiente de cobro',
  PAGADO: 'Recibido',
  ANULADO: 'Anulado',
};

/** `vista` ajusta el texto: el comercio "paga", el repartidor "cobra". */
export default function BadgeEstadoPago({ estado, vista = 'comercio' }) {
  const etiquetas = vista === 'repartidor' ? ETIQUETAS_REPARTIDOR : ETIQUETAS_COMERCIO;
  return (
    <span className={`badge-estado ${CLASES[estado] || 'badge-default'}`}>
      {etiquetas[estado] || estado}
    </span>
  );
}
