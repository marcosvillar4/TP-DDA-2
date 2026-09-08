import '../styles/EstadoPedidos.css';

const MAX = 168;

const estados = [
  { label: 'Pendiente',   count: 47,  color: 'var(--color-amber)' },
  { label: 'Preparando',  count: 35,  color: 'var(--color-orange)' },
  { label: 'En tránsito', count: 89,  color: 'var(--color-violet)' },
  { label: 'Entregado',   count: 168, color: 'var(--color-green)' },
  { label: 'Cancelado',   count: 3,   color: 'var(--color-red)' },
];

export default function EstadoPedidos() {
  return (
    <div className="estado-card">
      <div className="estado-header">
        <h2 className="estado-title">Estado de pedidos</h2>
        <p className="estado-subtitle">342 pedidos en el sistema</p>
      </div>

      <div className="estado-chart">
        {estados.map(({ label, count, color }) => (
          <div key={label} className="estado-row">
            <span className="estado-row-label">{label}</span>
            <div className="estado-track">
              <div
                className="estado-fill"
                style={{ width: `${(count / MAX) * 100}%`, backgroundColor: color }}
              />
            </div>
          </div>
        ))}
      </div>

      <div className="estado-axis">
        {[0, 45, 90, 135, 180].map((n) => <span key={n}>{n}</span>)}
      </div>

      <div className="estado-legend">
        {estados.map(({ label, count, color }) => (
          <span key={label} className="estado-legend-item">
            <span className="estado-legend-dot" style={{ backgroundColor: color }} />
            {label} <strong>{count}</strong>
          </span>
        ))}
      </div>
    </div>
  );
}
