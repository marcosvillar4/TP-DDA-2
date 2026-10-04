import { ESTADOS_PEDIDO, ORDEN_ESTADOS_PEDIDO } from '../utils/pedidoEstado';
import '../styles/EstadoPedidos.css';

/** @param {{ conteo: Record<string, number>, alcance?: string }} props */
export default function EstadoPedidos({ conteo = {}, alcance = 'en el sistema' }) {
  const estados = ORDEN_ESTADOS_PEDIDO.map((estado) => ({
    label: ESTADOS_PEDIDO[estado].label,
    color: ESTADOS_PEDIDO[estado].color,
    count: conteo[estado] ?? 0,
  }));

  const total = estados.reduce((acc, e) => acc + e.count, 0);
  const max = Math.max(...estados.map((e) => e.count), 1);
  const ticks = [0, 1, 2, 3, 4].map((i) => Math.round((max * i) / 4));

  return (
    <div className="estado-card">
      <div className="estado-header">
        <h2 className="estado-title">Estado de pedidos</h2>
        <p className="estado-subtitle">
          {total} pedido{total !== 1 ? 's' : ''} {alcance}
        </p>
      </div>

      <div className="estado-chart">
        {estados.map(({ label, count, color }) => (
          <div key={label} className="estado-row">
            <span className="estado-row-label">{label}</span>
            <div className="estado-track">
              <div
                className="estado-fill"
                style={{ width: `${(count / max) * 100}%`, backgroundColor: color }}
              />
            </div>
          </div>
        ))}
      </div>

      <div className="estado-axis">
        {ticks.map((n, i) => <span key={i}>{n}</span>)}
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
