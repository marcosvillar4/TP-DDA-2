import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { CreditCard, Wallet, CheckCircle2, Clock } from 'lucide-react';
import Swal from 'sweetalert2';
import { getPagosComercio } from '../../api/pagosApi';
import { formatMoneda } from '../pedidos/utils/formatMoneda';
import BadgeEstado from '../pedidos/components/BadgeEstado';
import BadgeEstadoPago from './components/BadgeEstadoPago';
import PagoModal from './components/PagoModal';
import './styles/Pagos.css';

const TABS = [
  { key: 'PENDIENTES', label: 'Pendientes' },
  { key: 'HISTORIAL', label: 'Historial' },
];

const fmtFecha = (f) => (f ? new Date(f).toLocaleString('es-AR') : '—');
const sumar = (pagos) => pagos.reduce((acc, p) => acc + Number(p.monto || 0), 0);

/** Pantalla "Pagos y Cobranzas" del COMERCIO: lo que debe y lo que ya pagó. */
export default function ComercioPagos() {
  const [pagos, setPagos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('PENDIENTES');
  const [pagando, setPagando] = useState(null);
  const [recarga, setRecarga] = useState(0); // se incrementa para volver a pedir los pagos

  useEffect(() => {
    let activo = true;
    getPagosComercio()
      .then((data) => { if (activo) { setPagos(data); setError(''); } })
      .catch((err) => { if (activo) setError(err.message || 'No se pudieron cargar los pagos.'); })
      .finally(() => { if (activo) setLoading(false); });
    return () => { activo = false; };
  }, [recarga]);

  const pendientes = useMemo(() => pagos.filter((p) => p.estado === 'PENDIENTE'), [pagos]);
  const pagados = useMemo(() => pagos.filter((p) => p.estado === 'PAGADO'), [pagos]);
  const historial = useMemo(() => pagos.filter((p) => p.estado !== 'PENDIENTE'), [pagos]);
  const visibles = tab === 'PENDIENTES' ? pendientes : historial;

  const handlePagado = (pago) => {
    setPagando(null);
    setRecarga((n) => n + 1);
    Swal.fire({
      icon: 'success',
      title: 'Pago realizado',
      html: `Pedido LOG-${pago.pedidoId}<br/>Operación <b>${pago.referenciaOperacion}</b>`,
      confirmButtonColor: '#16223f',
    });
  };

  return (
    <div className="pagos-page">
      <div className="pagos-header">
        <div>
          <h1 className="pagos-title">Pagos y Cobranzas</h1>
          <p className="pagos-subtitle">Costo de envío de tus pedidos</p>
        </div>
      </div>

      <div className="pagos-kpis">
        <div className="pagos-kpi pagos-kpi-pendiente">
          <Wallet size={20} />
          <div><span>Total pendiente</span><strong>{formatMoneda(sumar(pendientes))}</strong></div>
        </div>
        <div className="pagos-kpi">
          <Clock size={20} />
          <div><span>Pagos pendientes</span><strong>{pendientes.length}</strong></div>
        </div>
        <div className="pagos-kpi pagos-kpi-ok">
          <CheckCircle2 size={20} />
          <div><span>Total pagado</span><strong>{formatMoneda(sumar(pagados))}</strong></div>
        </div>
      </div>

      <div className="pagos-card">
        <div className="pagos-tabs" role="tablist">
          {TABS.map((t) => (
            <button
              key={t.key}
              role="tab"
              aria-selected={tab === t.key}
              className={tab === t.key ? 'pagos-tab pagos-tab-activo' : 'pagos-tab'}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {error && <div className="pagos-error-servidor" role="alert">{error}</div>}

        <div className="pagos-table-responsive">
          <table className="pagos-table">
            <thead>
              <tr>
                <th>Pedido</th>
                <th>Destino</th>
                <th>Estado del pedido</th>
                <th>Repartidor</th>
                <th>Monto</th>
                <th>Estado del pago</th>
                <th>{tab === 'PENDIENTES' ? 'Generado' : 'Fecha de pago'}</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="8" className="pagos-vacio">Cargando pagos…</td></tr>
              ) : visibles.length === 0 ? (
                <tr><td colSpan="8" className="pagos-vacio">
                  {tab === 'PENDIENTES' ? 'No tenés pagos pendientes.' : 'Todavía no hay pagos en el historial.'}
                </td></tr>
              ) : visibles.map((p) => (
                <tr key={p.id}>
                  <td className="pagos-col-id"><Link to={`/pedidos/${p.pedidoId}`}>LOG-{p.pedidoId}</Link></td>
                  <td>{p.direccionDestino}</td>
                  <td><BadgeEstado estado={p.pedidoEstado} /></td>
                  <td>{p.repartidorNombre || 'Sin asignar'}</td>
                  <td className="pagos-col-monto">{formatMoneda(p.monto)}</td>
                  <td>
                    <BadgeEstadoPago estado={p.estado} />
                    {p.estado === 'PAGADO' && p.referenciaOperacion && (
                      <small className="pagos-ref">
                        {p.referenciaOperacion}{p.tarjetaUltimos4 ? ` · ••••${p.tarjetaUltimos4}` : ''}
                      </small>
                    )}
                  </td>
                  <td className="pagos-col-fecha">{fmtFecha(tab === 'PENDIENTES' ? p.fechaCreacion : (p.fechaPago || p.fechaCreacion))}</td>
                  <td>
                    {p.estado === 'PENDIENTE' && (
                      <button className="pagos-btn-primario pagos-btn-chico" onClick={() => setPagando(p)}>
                        <CreditCard size={14} /> Pagar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {pagando && (
        <PagoModal pago={pagando} onClose={() => setPagando(null)} onPagado={handlePagado} />
      )}
    </div>
  );
}
