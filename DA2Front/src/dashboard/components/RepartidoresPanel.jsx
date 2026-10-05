import '../styles/RepartidoresPanel.css';

const CANTIDAD = 6;

const ESTADO_UI = {
  DISPONIBLE:    { texto: () => 'Disponible',   clase: 'rep-disponible', color: '#10b981' },
  EN_ENTREGA:    { texto: (r) => (r.pedidoActual ? `Pedido #${r.pedidoActual.id}` : 'En entrega'), clase: 'rep-activo', color: '#3b82f6' },
  NO_DISPONIBLE: { texto: () => 'No disponible', clase: 'rep-inactivo',  color: '#94a3b8' },
};

function iniciales(nombre = '') {
  return nombre
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('');
}

/** @param {{ repartidores: Array }} props  RepartidorResponseDTO[] */
export default function RepartidoresPanel({ repartidores = [] }) {
  const disponibles = repartidores.filter((r) => r.activo && r.estado === 'DISPONIBLE').length;
  const enEntrega = repartidores.filter((r) => r.estado === 'EN_ENTREGA').length;

  // Primero los que están trabajando, después los disponibles, al final el resto.
  const orden = { EN_ENTREGA: 0, DISPONIBLE: 1, NO_DISPONIBLE: 2 };
  const visibles = [...repartidores]
    .sort((a, b) => (orden[a.estado] ?? 3) - (orden[b.estado] ?? 3))
    .slice(0, CANTIDAD);

  return (
    <div className="repartidores-card">
      <div className="repartidores-header">
        <h2 className="repartidores-title">Repartidores</h2>
        <div className="repartidores-legend">
          <span className="rep-dot rep-dot-disp" /> {disponibles} disponible{disponibles !== 1 ? 's' : ''}
          <span className="rep-dot rep-dot-act" style={{ marginLeft: '0.5rem' }} /> {enEntrega} en entrega
        </div>
      </div>

      <ul className="repartidores-list">
        {visibles.map((r) => {
          const ui = ESTADO_UI[r.estado] ?? ESTADO_UI.NO_DISPONIBLE;
          return (
            <li key={r.id} className="repartidor-item">
              <div className="repartidor-avatar" style={{ backgroundColor: ui.color }}>
                {iniciales(r.nombreCompleto)}
              </div>
              <div className="repartidor-info">
                <span className="repartidor-nombre">{r.nombreCompleto}</span>
                <span className="repartidor-zona">{r.zona}</span>
              </div>
              <span className={`repartidor-estado ${ui.clase}`}>{ui.texto(r)}</span>
            </li>
          );
        })}
        {visibles.length === 0 && (
          <li className="repartidor-item">
            <span className="repartidor-zona">Todavía no hay repartidores registrados.</span>
          </li>
        )}
      </ul>
    </div>
  );
}
