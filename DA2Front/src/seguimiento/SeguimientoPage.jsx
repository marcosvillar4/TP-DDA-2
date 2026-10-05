import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MapPin, CheckCircle2, Warehouse, Store } from 'lucide-react';
import { getPedidoById } from '../../api/pedidosApi';
import { getComercios } from '../../api/inventarioApi';
import BadgeEstado from '../pedidos/components/BadgeEstado';
import './styles/SeguimientoPage.css';

const STEPS_ORDER = ['PENDIENTE_COTIZACION', 'ASIGNADO', 'EN_CAMINO', 'ENTREGADO'];

export default function SeguimientoPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchInput, setSearchInput] = useState('');
  
  const [pedido, setPedido] = useState(null);
  const [comercioNombre, setComercioNombre] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (!searchInput.trim()) return;
    navigate(`/seguimiento/${searchInput.trim()}`);
  };

  useEffect(() => {
    if (id) {
      setLoading(true);
      setError(false);
      setPedido(null);
      
      const cleanId = id.replace('LOG-', ''); // allow users to type LOG-1
      
      Promise.all([getPedidoById(cleanId), getComercios()])
        .then(([pedidoData, comerciosData]) => {
          setPedido(pedidoData);
          const comercio = comerciosData.find(c => c.id === pedidoData.comercioId);
          setComercioNombre(comercio ? comercio.nombreComercial : `Comercio ${pedidoData.comercioId}`);
        })
        .catch(err => {
          console.error(err);
          setError(true);
        })
        .finally(() => setLoading(false));
    }
  }, [id]);

  const recentSearches = ['1', '2'];

  let currentStepIndex = 1;
  let statusColorClass = 'status-navy';
  let isNotFound = error;
  let historialUI = [];

  if (pedido) {
    currentStepIndex = STEPS_ORDER.indexOf(pedido.estado) + 1;
    if (pedido.estado === 'CANCELADO') currentStepIndex = 5;
    if (currentStepIndex < 1) currentStepIndex = 1;

    switch (pedido.estado) {
      case 'PENDIENTE_COTIZACION': statusColorClass = 'status-amber'; break;
      case 'EN_CAMINO': statusColorClass = 'status-violet'; break;
      case 'ENTREGADO': statusColorClass = 'status-green'; break;
      case 'ASIGNADO': statusColorClass = 'status-blue'; break;
      case 'CANCELADO': statusColorClass = 'status-red'; break;
      default: statusColorClass = 'status-navy';
    }

    // reverse so chronological order is top-down
    historialUI = (pedido.historial || []).map((h, i) => {
      const isCurrent = i === 0;
      return {
        estado: h.estado,
        fecha: new Date(h.fechaHora).toLocaleString(),
        ubicacion: 'Sistema',
        status: isCurrent ? 'current' : 'completed'
      };
    }).reverse();
  }

  return (
    <div className="seguimiento-container">
      <div className="seguimiento-search-card">
        <div className="seguimiento-icon-wrapper">
          <MapPin size={28} />
        </div>
        <h1 className="seguimiento-title">Seguimiento de pedidos</h1>
        <p className="seguimiento-subtitle">
          Ingresá el ID del pedido para consultar su estado y trayectoria de entrega.
        </p>
        
        <form className="seguimiento-input-group" onSubmit={handleSearch}>
          <input 
            type="text" 
            className="seguimiento-input" 
            placeholder="Ej: 1"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="seguimiento-btn">Buscar</button>
        </form>

        <div className="seguimiento-recent">
          <p className="seguimiento-recent-title">Búsquedas recientes:</p>
          <div className="seguimiento-chips">
            {recentSearches.map(searchId => (
              <button 
                key={searchId} 
                className="seguimiento-chip"
                onClick={() => navigate(`/seguimiento/${searchId}`)}
                type="button"
              >
                LOG-{searchId}
              </button>
            ))}
          </div>
        </div>
      </div>

      {loading && (
        <div className="seguimiento-card" style={{ width: '100%', maxWidth: '900px', textAlign: 'center' }}>
          <p>Buscando pedido...</p>
        </div>
      )}

      {!loading && isNotFound && (
        <div className="seguimiento-card" style={{ width: '100%', maxWidth: '900px', textAlign: 'center' }}>
          <p>No se encontró ningún pedido con el ID: <strong>{id}</strong></p>
        </div>
      )}

      {/* Result Section */}
      {!loading && pedido && (
        <div className="seguimiento-result-wrapper">
          {/* Top Status Card */}
          <div className={`seguimiento-card seguimiento-status-card ${statusColorClass}`}>
            <div className="status-header">
              <div className="status-info-left">
                <span className="status-label">NÚMERO DE SEGUIMIENTO</span>
                <h2 className="status-id">LOG-{pedido.id}</h2>
                <span className="status-route">
                  {comercioNombre}
                </span>
              </div>
              <div className="status-info-right">
                <BadgeEstado estado={pedido.estado} />
                {pedido.estado !== 'CANCELADO' && (
                  <span className="status-step-text">Paso {currentStepIndex} de 4</span>
                )}
                {pedido.fechaCreacion && (
                  <span className="status-date">Creado: {new Date(pedido.fechaCreacion).toLocaleString()}</span>
                )}
              </div>
            </div>

            {pedido.estado !== 'CANCELADO' && (
              <div className="status-progress-bar">
                {[1, 2, 3, 4].map(step => {
                  let segmentClass = 'pending';
                  if (step < currentStepIndex) segmentClass = 'completed';
                  else if (step === currentStepIndex) segmentClass = 'current';
                  
                  return <div key={step} className={`progress-segment ${segmentClass}`} />
                })}
              </div>
            )}
            {pedido.estado !== 'CANCELADO' && (
              <div className="status-progress-labels">
                <span>PENDIENTE_COTIZACION</span>
                <span>Entregado</span>
              </div>
            )}
          </div>

          <div className="seguimiento-detail-grid">
            {/* Left: History Stepper */}
            <div className="seguimiento-card history-section">
              <h3 className="history-title">Historial de seguimiento</h3>
              <p className="history-subtitle">Trayectoria completa del pedido</p>
              
              <div className="history-stepper">
                {historialUI.map((step, index) => (
                  <div key={index} className={`history-step ${step.status}`}>
                    <div className="history-icon-wrapper">
                      {step.status === 'completed' ? (
                        <CheckCircle2 size={16} className="history-icon-completed" />
                      ) : (
                        <div className="history-icon-dot" />
                      )}
                    </div>
                    <div className="history-content">
                      <div className="history-header">
                        <span className="history-status-name">{step.estado}</span>
                        {step.status === 'current' && <span className="history-badge-actual">ACTUAL</span>}
                      </div>
                      {step.fecha && <span className="history-date">{step.fecha}</span>}
                      {step.ubicacion && <span className="history-location">{step.ubicacion}</span>}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right: Info Cards */}
            <div className="info-cards-col">
              
              <div className="seguimiento-card info-card">
                <span className="info-card-label">DIRECCIÓN DE ENTREGA</span>
                <div className="info-card-content">
                  <MapPin size={16} className="info-icon-red" />
                  <span>{pedido.direccionDestino}</span>
                </div>
              </div>

              {/* Conditional Rendering for Repartidor */}
              {['ASIGNADO', 'EN_CAMINO'].includes(pedido.estado) && pedido.repartidorNombre && (
                <div className="seguimiento-card info-card">
                  <span className="info-card-label">REPARTIDOR ASIGNADO</span>
                  <div className="info-card-content">
                    <div className="repartidor-avatar">
                      {pedido.repartidorNombre.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
                    </div>
                    <div className="repartidor-details">
                      <span className="repartidor-name">{pedido.repartidorNombre}</span>
                      <span className="repartidor-status">En ruta activa</span>
                    </div>
                  </div>
                </div>
              )}

              <div className="seguimiento-card info-card">
                <span className="info-card-label">COMERCIO</span>
                <div className="info-card-stack">
                  <div className="info-stack-item">
                    <Store size={16} className="info-icon-navy" />
                    <span style={{ fontSize: '0.875rem', color: 'var(--color-slate-700)' }}>{comercioNombre}</span>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      )}
    </div>
  );
}




