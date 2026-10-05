import { ESTADOS_PEDIDO } from '../utils/pedidoEstado';
import '../styles/PedidosRecientes.css';

const CANTIDAD = 7;

/** Entregas finalizadas del repartidor (EntregaHistorialDTO[]). */
export default function HistorialEntregas({ historial = [] }) {
  const recientes = [...historial].sort((a, b) => b.pedidoId - a.pedidoId).slice(0, CANTIDAD);

  return (
    <div className="pedidos-card">
      <div className="pedidos-header">
        <div>
          <h2 className="pedidos-title">Historial de entregas</h2>
          <p className="pedidos-subtitle">Tus últimas entregas finalizadas</p>
        </div>
      </div>
      <div className="pedidos-table-wrapper">
        <table className="pedidos-table">
          <thead>
            <tr>
              <th>ID PEDIDO</th>
              <th>DIRECCIÓN DE ENTREGA</th>
              <th>RESULTADO</th>
            </tr>
          </thead>
          <tbody>
            {recientes.map((h) => (
              <tr key={h.pedidoId}>
                <td className="pedido-id">#{h.pedidoId}</td>
                <td>{h.direccionEntrega}</td>
                <td>
                  <span className={`pedido-badge ${ESTADOS_PEDIDO[h.resultado]?.badge ?? ''}`}>
                    {ESTADOS_PEDIDO[h.resultado]?.label ?? h.resultado}
                  </span>
                </td>
              </tr>
            ))}
            {recientes.length === 0 && (
              <tr>
                <td colSpan={3} className="pedido-fecha">Todavía no completaste entregas.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
