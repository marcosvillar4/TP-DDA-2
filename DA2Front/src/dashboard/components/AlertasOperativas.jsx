import { useState } from 'react';
import { AlertCircle, AlertTriangle } from 'lucide-react';
import '../styles/AlertasOperativas.css';

const VISIBLES = 5;

/** @param {{ alertas: Array<{nivel, tipo, titulo, desc}> }} props  ver utils/alertas.js */
export default function AlertasOperativas({ alertas = [] }) {
  const [verTodas, setVerTodas] = useState(false);

  const criticas = alertas.filter((a) => a.nivel === 'critico').length;
  const advertencias = alertas.length - criticas;
  const visibles = verTodas ? alertas : alertas.slice(0, VISIBLES);

  return (
    <div className="alertas-card">
      <div className="alertas-header">
        <div>
          <h2 className="alertas-title">Alertas operativas</h2>
          <p className="alertas-subtitle">
            <span className="alertas-critico-count">
              {criticas} crítica{criticas !== 1 ? 's' : ''}
            </span>{' '}
            · {advertencias} advertencia{advertencias !== 1 ? 's' : ''}
          </p>
        </div>
        {alertas.length > VISIBLES && (
          <button className="alertas-ver-btn" onClick={() => setVerTodas((v) => !v)}>
            {verTodas ? 'Ver menos' : 'Ver todas'}
          </button>
        )}
      </div>

      <ul className="alertas-list">
        {visibles.map((a, i) => (
          <li key={i} className={`alerta-item alerta-${a.nivel}`}>
            <div className="alerta-icon">
              {a.nivel === 'critico'
                ? <AlertCircle size={15} />
                : <AlertTriangle size={15} />}
            </div>
            <div className="alerta-body">
              <div className="alerta-head">
                <span className={`alerta-badge alerta-badge-${a.nivel}`}>{a.tipo}</span>
                <strong className="alerta-titulo">{a.titulo}</strong>
              </div>
              <p className="alerta-desc">{a.desc}</p>
            </div>
          </li>
        ))}
        {alertas.length === 0 && (
          <li className="alerta-item">
            <div className="alerta-body">
              <p className="alerta-desc">No hay alertas activas.</p>
            </div>
          </li>
        )}
      </ul>
    </div>
  );
}
