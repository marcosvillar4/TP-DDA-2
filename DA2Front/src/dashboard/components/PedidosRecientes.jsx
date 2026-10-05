import { useNavigate } from 'react-router-dom';
import { ESTADOS_PEDIDO } from '../utils/pedidoEstado';
import '../styles/PedidosRecientes.css';

const CANTIDAD = 7;

/**
 * @param {{
 *   pedidos: Array,            PedidoResponseDTO (opcionalmente con comercioNombre)
 *   mostrarComercio?: boolean,
 *   verTodosPath?: string      si se omite, no se muestra el botón
 * }} props
 */
export default function PedidosRecientes({ pedidos = [], mostrarComercio = false, verTodosPath }) {
  const navigate = useNavigate();

  // El id es autoincremental: los más nuevos tienen el id más alto.
  const recientes = [...pedidos].sort((a, b) => b.id - a.id).slice(0, CANTIDAD);
  const columnas = mostrarComercio ? 5 : 4;

  return (
    <div className="pedidos-card">
      <div className="pedidos-header">
        <div>
          <h2 className="pedidos-title">Pedidos recientes</h2>
          <p className="pedidos-subtitle">Últimas operaciones registradas</p>
        </div>
        {verTodosPath && (
          <button className="pedidos-ver-btn" onClick={() => navigate(verTodosPath)}>
            Ver todos
          </button>
        )}
      </div>
      <div className="pedidos-table-wrapper">
        <table className="pedidos-table">
          <thead>
            <tr>
              <th>ID PEDIDO</th>
              {mostrarComercio && <th>COMERCIO</th>}
              <th>DESTINO</th>
              <th>ESTADO</th>
              <th>REPARTIDOR</th>
            </tr>
          </thead>
          <tbody>
            {recientes.map((p) => (
              <tr key={p.id}>
                <td className="pedido-id">#{p.id}</td>
                {mostrarComercio && <td>{p.comercioNombre ?? `Comercio Nº ${p.comercioId}`}</td>}
                <td>{p.direccionDestino}</td>
                <td>
                  <span className={`pedido-badge ${ESTADOS_PEDIDO[p.estado]?.badge ?? ''}`}>
                    {ESTADOS_PEDIDO[p.estado]?.label ?? p.estado}
                  </span>
                </td>
                <td>{p.repartidorNombre ?? '—'}</td>
              </tr>
            ))}
            {recientes.length === 0 && (
              <tr>
                <td colSpan={columnas} className="pedido-fecha">Todavía no hay pedidos.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
