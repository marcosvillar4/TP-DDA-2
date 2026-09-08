import { AlertCircle, AlertTriangle } from 'lucide-react';
import '../styles/AlertasOperativas.css';

const alertas = [
  { tipo: 'CRÍTICO',     titulo: 'Sin stock',       desc: 'Urban Shoes – Depósito Palermo sin stock disponible',               tiempo: 'hace 12 min', nivel: 'critico' },
  { tipo: 'CRÍTICO',     titulo: 'Pedido demorado', desc: 'LOG-0021 lleva 4h de retraso – zona Belgrano',                      tiempo: 'hace 45 min', nivel: 'critico' },
  { tipo: 'ADVERTENCIA', titulo: 'Stock bajo',      desc: 'TecnoStore – Dep. Caballito: 3 unidades restantes',                 tiempo: 'hace 1h',     nivel: 'advertencia' },
  { tipo: 'ADVERTENCIA', titulo: 'Gran volumen',    desc: 'Casa Norte – Dep. Belgrano: 128 paquetes pendientes de despacho',   tiempo: 'hace 2h',     nivel: 'advertencia' },
  { tipo: 'ADVERTENCIA', titulo: 'Stock bajo',      desc: 'Casa Norte – Depósito Caballito: 5 unidades restantes',             tiempo: 'hace 3h',     nivel: 'advertencia' },
];

export default function AlertasOperativas() {
  return (
    <div className="alertas-card">
      <div className="alertas-header">
        <div>
          <h2 className="alertas-title">Alertas operativas</h2>
          <p className="alertas-subtitle">
            <span className="alertas-critico-count">2 críticas</span> · 3 advertencias
          </p>
        </div>
        <button className="alertas-ver-btn">Ver todas</button>
      </div>

      <ul className="alertas-list">
        {alertas.map((a, i) => (
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
            <span className="alerta-tiempo">{a.tiempo}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
