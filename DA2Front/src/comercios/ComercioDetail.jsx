import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, User, Mail, Phone, MapPin, Edit2, Plus, Warehouse } from 'lucide-react';
import { useComercioDetail } from '../../hooks/useComercioDetail';
import BadgeComercio from './components/BadgeComercio';
import BadgeEstado from '../pedidos/components/BadgeEstado'; // Reuse for recent orders
import { labelEstadoComercio } from './utils/comercioEstado';
import './styles/ComercioDetail.css';

// EstadoPedido del backend → etiqueta que entiende BadgeEstado.
const ESTADO_PEDIDO_LABEL = {
  CREADO: 'Pendiente',
  ASIGNADO: 'Preparando',
  EN_CAMINO: 'En tránsito',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

const PEDIDOS_RECIENTES = 10;

export default function ComercioDetail({ propio = false }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { comercio, pedidos, depositos, loading, error } = useComercioDetail(id, propio);

  const rutaVolver = propio ? '/dashboard' : '/comercios';
  const textoVolver = propio ? 'Volver al Dashboard' : 'Volver a Comercios';

  if (loading) {
    return <div className="comercio-detail-page"><p>Cargando comercio...</p></div>;
  }

  if (error || !comercio) {
    return (
      <div className="comercio-detail-page">
        <button className="comercio-btn-volver" onClick={() => navigate(rutaVolver)}>
          <ArrowLeft size={16} /> {textoVolver}
        </button>
        <p>{error || 'No se encontró el comercio.'}</p>
      </div>
    );
  }

  const totales = pedidos.length;
  const pendientes = pedidos.filter((p) => p.estado === 'CREADO').length;
  const enCurso = pedidos.filter((p) => p.estado === 'ASIGNADO' || p.estado === 'EN_CAMINO').length;

  // Más recientes primero (el id es autoincremental).
  const pedidosRecientes = [...pedidos]
    .sort((a, b) => b.id - a.id)
    .slice(0, PEDIDOS_RECIENTES);

  return (
    <div className="comercio-detail-page">
      {!propio && (
        <button className="comercio-btn-volver" onClick={() => navigate(rutaVolver)}>
          <ArrowLeft size={16} /> {textoVolver}
        </button>
      )}

      <div className="comercio-detail-header">
        <div className="comercio-detail-title-group">
          <h1 className="comercio-detail-title">{comercio.nombreComercial}</h1>
          <BadgeComercio estado={comercio.estado} />
          <span className="comercio-detail-cuit">CUIT {comercio.cuit}</span>
        </div>
        <div className="comercio-detail-actions">
          <button className="comercio-btn-outline" onClick={() => navigate(`/comercios/${comercio.id}/editar`)}>
            <Edit2 size={16} /> Editar
          </button>
        </div>
      </div>

      <div className="comercio-detail-grid">
        {/* Left Col: Info */}
        <div className="comercio-info-col">
          <div className="comercio-card">
            <h3 className="comercio-card-title">Información de contacto</h3>

            <div className="comercio-info-list">
              <div className="comercio-info-item">
                <User size={16} className="info-icon" />
                <div className="info-content">
                  <span className="info-label">RESPONSABLE</span>
                  <span className="info-value">{comercio.responsable ?? '—'}</span>
                </div>
              </div>

              <div className="comercio-info-item">
                <Mail size={16} className="info-icon" />
                <div className="info-content">
                  <span className="info-label">EMAIL</span>
                  <span className="info-value">{comercio.email}</span>
                </div>
              </div>

              <div className="comercio-info-item">
                <Phone size={16} className="info-icon" />
                <div className="info-content">
                  <span className="info-label">TELÉFONO</span>
                  <span className="info-value">{comercio.telefono}</span>
                </div>
              </div>

              <div className="comercio-info-item">
                <MapPin size={16} className="info-icon" />
                <div className="info-content">
                  <span className="info-label">DIRECCIÓN</span>
                  <span className="info-value">{comercio.direccion}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Col: KPIs + Depósitos + Recientes */}
        <div className="comercio-main-col">

          <div className="comercio-kpi-grid">
            <div className="comercio-kpi-card">
              <span className="kpi-value">{totales}</span>
              <span className="kpi-label">Pedidos totales</span>
            </div>
            <div className="comercio-kpi-card">
              <span className="kpi-value">{enCurso}</span>
              <span className="kpi-label">En curso</span>
            </div>
            <div className="comercio-kpi-card">
              <span className="kpi-value">{pendientes}</span>
              <span className="kpi-label">Pendientes</span>
            </div>
          </div>

          <div className="comercio-card">
            <div className="comercio-card-header comercio-card-header-row">
              <div>
                <h3 className="comercio-card-title">Depósitos vinculados</h3>
                <span className="comercio-card-subtitle">
                  {depositos.length} depósito{depositos.length !== 1 ? 's' : ''} de {comercio.nombreComercial}
                </span>
              </div>
              {propio && (
                <button className="comercio-btn-outline" onClick={() => navigate('/mis-depositos')}>
                  <Plus size={16} /> Agregar depósito
                </button>
              )}
            </div>

            {depositos.length === 0 ? (
              <p className="comercio-empty-text">Todavía no hay depósitos vinculados.</p>
            ) : (
              <ul className="comercio-depositos-list">
                {depositos.map((d) => (
                  <li key={d.id} className="comercio-deposito-item">
                    <Warehouse size={16} className="info-icon" />
                    <div className="info-content">
                      <span className="info-value comercio-deposito-nombre">{d.nombre}</span>
                      <span className="comercio-card-subtitle">{d.direccion}</span>
                    </div>
                    {d.usuarioEstado && (
                      <span className="comercio-deposito-estado">
                        Cuenta: {labelEstadoComercio(d.usuarioEstado)}
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="comercio-card">
            <div className="comercio-card-header">
              <h3 className="comercio-card-title">Pedidos recientes</h3>
              <span className="comercio-card-subtitle">
                {totales} pedido{totales !== 1 ? 's' : ''} de {comercio.nombreComercial} en el sistema
              </span>
            </div>

            <div className="comercio-table-responsive">
              <table className="comercio-pedidos-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>DIRECCIÓN DE DESTINO</th>
                    <th>ESTADO</th>
                    <th>REPARTIDOR</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidosRecientes.map((p) => (
                    <tr key={p.id}>
                      <td className="col-id-small">#{p.id}</td>
                      <td>{p.direccionDestino}</td>
                      <td><BadgeEstado estado={ESTADO_PEDIDO_LABEL[p.estado] ?? p.estado} /></td>
                      <td>{p.repartidorNombre ?? '—'}</td>
                    </tr>
                  ))}
                  {pedidosRecientes.length === 0 && (
                    <tr>
                      <td colSpan="4" className="comercio-empty-text">Este comercio todavía no tiene pedidos.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
