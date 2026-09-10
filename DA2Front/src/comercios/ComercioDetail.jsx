import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, User, Mail, Phone, MapPin, Edit2 } from 'lucide-react';
import { comerciosMock, pedidosRecientesMock } from './mockData';
import BadgeComercio from './components/BadgeComercio';
import BadgeEstado from '../pedidos/components/BadgeEstado'; // Reuse for recent orders
import './styles/ComercioDetail.css';

export default function ComercioDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const comercio = comerciosMock.find(c => c.id === id) || comerciosMock[0];

  return (
    <div className="comercio-detail-page">
      <button className="comercio-btn-volver" onClick={() => navigate('/comercios')}>
        <ArrowLeft size={16} /> Volver a Comercios
      </button>

      <div className="comercio-detail-header">
        <div className="comercio-detail-title-group">
          <h1 className="comercio-detail-title">{comercio.nombre}</h1>
          <BadgeComercio estado={comercio.estado} />
          <span className="comercio-detail-cuit">CUIT {comercio.cuit}</span>
        </div>
        <div className="comercio-detail-actions">
          <button className="comercio-btn-outline" onClick={() => navigate(`/comercios/${id}/editar`)}>
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
                  <span className="info-value">{comercio.responsable}</span>
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

        {/* Right Col: KPIs + Recentes */}
        <div className="comercio-main-col">
          
          <div className="comercio-kpi-grid">
            <div className="comercio-kpi-card">
              <span className="kpi-value">{comercio.stats.totales}</span>
              <span className="kpi-label">Pedidos totales</span>
            </div>
            <div className="comercio-kpi-card">
              <span className="kpi-value">{comercio.stats.mes}</span>
              <span className="kpi-label">Este mes</span>
            </div>
            <div className="comercio-kpi-card">
              <span className="kpi-value">{comercio.stats.pendientes}</span>
              <span className="kpi-label">Pendientes</span>
            </div>
          </div>

          <div className="comercio-card">
            <div className="comercio-card-header">
              <h3 className="comercio-card-title">Pedidos recientes</h3>
              <span className="comercio-card-subtitle">{pedidosRecientesMock.length} pedidos de {comercio.nombre} en el sistema</span>
            </div>
            
            <div className="comercio-table-responsive">
              <table className="comercio-pedidos-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>DESTINATARIO</th>
                    <th>ESTADO</th>
                    <th>REPARTIDOR</th>
                    <th>FECHA</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidosRecientesMock.map(p => (
                    <tr key={p.id}>
                      <td className="col-id-small">{p.id}</td>
                      <td>{p.destinatario}</td>
                      <td><BadgeEstado estado={p.estado} /></td>
                      <td>{p.repartidor}</td>
                      <td className="col-fecha">{p.fecha}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
