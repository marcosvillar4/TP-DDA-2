import '../styles/PedidosRecientes.css';

const pedidos = [
  { id: 'LOG-0031', comercio: 'Urban Shoes',  destinatario: 'María García',    estado: 'En tránsito', repartidor: 'Carlos Ruiz',   fecha: '24/08/2026' },
  { id: 'LOG-0030', comercio: 'TecnoStore',   destinatario: 'Juan Pérez',      estado: 'Entregado',   repartidor: 'Ana López',     fecha: '24/08/2026' },
  { id: 'LOG-0029', comercio: 'Casa Norte',   destinatario: 'Lucía Martínez',  estado: 'Pendiente',   repartidor: '—',             fecha: '23/08/2026' },
  { id: 'LOG-0028', comercio: 'Urban Shoes',  destinatario: 'Roberto Silva',   estado: 'Preparando',  repartidor: 'Miguel Torres', fecha: '23/08/2026' },
  { id: 'LOG-0027', comercio: 'TecnoStore',   destinatario: 'Carmen Díaz',     estado: 'Cancelado',   repartidor: '—',             fecha: '22/08/2026' },
  { id: 'LOG-0026', comercio: 'Casa Norte',   destinatario: 'Diego Fernández', estado: 'Entregado',   repartidor: 'Pablo Gómez',  fecha: '22/08/2026' },
  { id: 'LOG-0025', comercio: 'Urban Shoes',  destinatario: 'Sofía Castro',    estado: 'En tránsito', repartidor: 'Carlos Ruiz',   fecha: '21/08/2026' },
];

const estadoClass = {
  'En tránsito': 'badge-transito',
  'Entregado':   'badge-entregado',
  'Pendiente':   'badge-pendiente',
  'Preparando':  'badge-preparando',
  'Cancelado':   'badge-cancelado',
};

export default function PedidosRecientes() {
  return (
    <div className="pedidos-card">
      <div className="pedidos-header">
        <div>
          <h2 className="pedidos-title">Pedidos recientes</h2>
          <p className="pedidos-subtitle">Últimas operaciones registradas</p>
        </div>
        <button className="pedidos-ver-btn">Ver todos</button>
      </div>
      <div className="pedidos-table-wrapper">
        <table className="pedidos-table">
          <thead>
            <tr>
              <th>ID PEDIDO</th>
              <th>COMERCIO</th>
              <th>DESTINATARIO</th>
              <th>ESTADO</th>
              <th>REPARTIDOR</th>
              <th>FECHA</th>
            </tr>
          </thead>
          <tbody>
            {pedidos.map((p) => (
              <tr key={p.id}>
                <td className="pedido-id">{p.id}</td>
                <td>{p.comercio}</td>
                <td>{p.destinatario}</td>
                <td>
                  <span className={`pedido-badge ${estadoClass[p.estado] ?? ''}`}>
                    {p.estado}
                  </span>
                </td>
                <td>{p.repartidor}</td>
                <td className="pedido-fecha">{p.fecha}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
