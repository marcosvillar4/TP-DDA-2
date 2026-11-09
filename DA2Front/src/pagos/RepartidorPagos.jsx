import { useEffect, useMemo, useState } from 'react';
import { Wallet, CheckCircle2 } from 'lucide-react';
import { getMisCobros } from '../../api/pagosApi';
import { formatMoneda } from '../pedidos/utils/formatMoneda';
import BadgeEstado from '../pedidos/components/BadgeEstado';
import BadgeEstadoPago from './components/BadgeEstadoPago';
import './styles/Pagos.css';

const FILTROS = [
  { key: 'TODOS', label: 'Todos' },
  { key: 'PENDIENTE', label: 'Pendientes de cobro' },
  { key: 'PAGADO', label: 'Recibidos' },
];

const METODOS = {
  TARJETA_CREDITO: 'Tarjeta de crédito',
  TARJETA_DEBITO: 'Tarjeta de débito',
  TRANSFERENCIA: 'Transferencia',
};

const fmtFecha = (f) => (f ? new Date(f).toLocaleString('es-AR') : '—');
const sumar = (pagos) => pagos.reduce((acc, p) => acc + Number(p.monto || 0), 0);

/** Pantalla "Pagos y Cobranzas" del REPARTIDOR: cobros recibidos y pendientes, por pedido. */
export default function RepartidorPagos() {
  const [pagos, setPagos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filtro, setFiltro] = useState('TODOS');

  useEffect(() => {
    let activo = true;
    getMisCobros()
      .then((data) => { if (activo) setPagos(data); })
      .catch((err) => { if (activo) setError(err.message || 'No se pudieron cargar tus cobros.'); })
      .finally(() => { if (activo) setLoading(false); });
    return () => { activo = false; };
  }, []);

  const pendientes = useMemo(() => pagos.filter((p) => p.estado === 'PENDIENTE'), [pagos]);
  const recibidos = useMemo(() => pagos.filter((p) => p.estado === 'PAGADO'), [pagos]);
  const visibles = filtro === 'TODOS' ? pagos : pagos.filter((p) => p.estado === filtro);

  return (
    <div className="pagos-page">
      <div className="pagos-header">
        <div>
          <h1 className="pagos-title">Pagos y Cobranzas</h1>
          <p className="pagos-subtitle">Lo que recibiste y lo que tenés pendiente de cobro, por pedido</p>
        </div>
      </div>

      <div className="pagos-kpis">
        <div className="pagos-kpi pagos-kpi-pendiente">
          <Wallet size={20} />
          <div><span>Pendiente de cobro</span><strong>{formatMoneda(sumar(pendientes))}</strong></div>
        </div>
        <div className="pagos-kpi pagos-kpi-ok">
          <CheckCircle2 size={20} />
          <div><span>Total recibido</span><strong>{formatMoneda(sumar(recibidos))}</strong></div>
        </div>
      </div>

      <div className="pagos-card">
        <div className="pagos-tabs" role="tablist">
          {FILTROS.map((f) => (
            <button
              key={f.key}
              role="tab"
              aria-selected={filtro === f.key}
              className={filtro === f.key ? 'pagos-tab pagos-tab-activo' : 'pagos-tab'}
              onClick={() => setFiltro(f.key)}
            >
              {f.label}
            </button>
          ))}
        </div>

        {error && <div className="pagos-error-servidor" role="alert">{error}</div>}

        <div className="pagos-table-responsive">
          <table className="pagos-table">
            <thead>
              <tr>
                <th>Pedido</th>
                <th>Comercio</th>
                <th>Destino</th>
                <th>Estado del pedido</th>
                <th>Monto</th>
                <th>Estado del cobro</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="7" className="pagos-vacio">Cargando cobros…</td></tr>
              ) : visibles.length === 0 ? (
                <tr><td colSpan="7" className="pagos-vacio">No hay cobros para mostrar.</td></tr>
              ) : visibles.map((p) => (
                <tr key={p.id}>
                  <td className="pagos-col-id">LOG-{p.pedidoId}</td>
                  <td>{p.comercioNombre || `Comercio ${p.comercioId}`}</td>
                  <td>{p.direccionDestino}</td>
                  <td><BadgeEstado estado={p.pedidoEstado} /></td>
                  <td className="pagos-col-monto">{formatMoneda(p.monto)}</td>
                  <td>
                    <BadgeEstadoPago estado={p.estado} vista="repartidor" />
                    {p.estado === 'PAGADO' && p.metodoPago && (
                      <small className="pagos-ref">{METODOS[p.metodoPago] || p.metodoPago}</small>
                    )}
                  </td>
                  <td className="pagos-col-fecha">
                    {fmtFecha(p.estado === 'PAGADO' ? p.fechaPago : p.fechaCreacion)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
