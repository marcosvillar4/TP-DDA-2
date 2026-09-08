import '../styles/RepartidoresPanel.css';

const repartidores = [
  { iniciales: 'CR', nombre: 'Carlos Ruiz',   zona: 'Palermo',  estado: '3 pedidos',  color: '#3b82f6' },
  { iniciales: 'AL', nombre: 'Ana López',     zona: 'Caballito', estado: 'Disponible', color: '#10b981' },
  { iniciales: 'MT', nombre: 'Miguel Torres', zona: 'Belgrano', estado: '2 pedidos',  color: '#8b5cf6' },
  { iniciales: 'PG', nombre: 'Pablo Gómez',   zona: 'Palermo',  estado: 'Disponible', color: '#10b981' },
  { iniciales: 'SR', nombre: 'Sandra Ríos',   zona: 'Caballito', estado: '4 pedidos',  color: '#3b82f6' },
];

export default function RepartidoresPanel() {
  return (
    <div className="repartidores-card">
      <div className="repartidores-header">
        <h2 className="repartidores-title">Repartidores</h2>
        <div className="repartidores-legend">
          <span className="rep-dot rep-dot-disp" /> 3 disponibles
          <span className="rep-dot rep-dot-act" style={{ marginLeft: '0.5rem' }} /> 5 en entrega
        </div>
      </div>

      <ul className="repartidores-list">
        {repartidores.map((r) => {
          const disp = r.estado === 'Disponible';
          return (
            <li key={r.nombre} className="repartidor-item">
              <div className="repartidor-avatar" style={{ backgroundColor: r.color }}>
                {r.iniciales}
              </div>
              <div className="repartidor-info">
                <span className="repartidor-nombre">{r.nombre}</span>
                <span className="repartidor-zona">{r.zona}</span>
              </div>
              <span className={`repartidor-estado ${disp ? 'rep-disponible' : 'rep-activo'}`}>
                {r.estado}
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
