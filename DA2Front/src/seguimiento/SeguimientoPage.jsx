import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MapPin, CheckCircle2, Warehouse, Store } from 'lucide-react';
import { pedidosMock } from '../pedidos/mockData';
import BadgeEstado from '../pedidos/components/BadgeEstado';
import './styles/SeguimientoPage.css';

const STEPS_ORDER = [
  'Pedido recibido',
  'En preparación',
  'Listo para despacho',
  'En tránsito',
  'Entregado'
];

export default function SeguimientoPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchInput, setSearchInput] = useState('');
  
  useEffect(() => {
    if (id) {
      setSearchInput(id);
    }
  }, [id]);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchInput.trim()) {
      navigate(`/seguimiento/${searchInput.trim()}`);
    }
  };

  const recentSearches = ['LOG-0031', 'LOG-0025', 'LOG-0023', 'LOG-0021'];

  // Check if we have a valid tracking ID to show
  const pedido = id ? pedidosMock.find(p => p.id === id) : null;
  const isNotFound = id && !pedido;

  // Determine current step index (1 to 5) based on history array
  let currentStepIndex = 1;
  let statusColorClass = 'status-navy'; // Default fallback
  
  if (pedido) {
    const currentHist = pedido.historial.find(h => h.status === 'current' || h.status === 'completed' && h.estado === pedido.estado);
    if (currentHist) {
      currentStepIndex = STEPS_ORDER.indexOf(currentHist.estado) + 1;
    } else {
      // fallback based on general status
      currentStepIndex = STEPS_ORDER.indexOf(pedido.estado) + 1;
    }
    if (currentStepIndex < 1) currentStepIndex = 1;

    switch (pedido.estado) {
      case 'Pendiente': statusColorClass = 'status-amber'; break;
      case 'En tránsito': statusColorClass = 'status-violet'; break;
      case 'Entregado': statusColorClass = 'status-green'; break;
      case 'Preparando': statusColorClass = 'status-blue'; break;
      case 'Cancelado': statusColorClass = 'status-red'; break;
      default: statusColorClass = 'status-navy';
    }
  }

  return (
    <div className="seguimiento-container">
      {/* Search Header Card */}
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
            placeholder="Ej: LOG-0031"
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
                {searchId}
              </button>
            ))}
          </div>
        </div>
      </div>

      {isNotFound && (
        <div className="seguimiento-card" style={{ width: '100%', maxWidth: '900px', textAlign: 'center' }}>
          <p>No se encontró ningún pedido con el ID: <strong>{id}</strong></p>
        </div>
      )}

      {/* Result Section */}
      {pedido && (
        <div className="seguimiento-result-wrapper">
          {/* Top Status Card */}
          <div className={`seguimiento-card seguimiento-status-card ${statusColorClass}`}>
            <div className="status-header">
              <div className="status-info-left">
                <span className="status-label">NÚMERO DE SEGUIMIENTO</span>
                <h2 className="status-id">{pedido.id}</h2>
                <span className="status-route">
                  {pedido.comercio.nombre} ➔ <strong>{pedido.destinatario}</strong>
                </span>
              </div>
              <div className="status-info-right">
                <BadgeEstado estado={pedido.estado} />
                <span className="status-step-text">Paso {currentStepIndex} de 5</span>
                <span className="status-date">Creado: {pedido.fecha}</span>
              </div>
            </div>

            <div className="status-progress-bar">
              {[1, 2, 3, 4, 5].map(step => {
                let segmentClass = 'pending';
                if (step < currentStepIndex) segmentClass = 'completed';
                else if (step === currentStepIndex) segmentClass = 'current';
                
                return <div key={step} className={`progress-segment ${segmentClass}`} />
              })}
            </div>
            <div className="status-progress-labels">
              <span>Recibido</span>
              <span>Entregado</span>
            </div>
          </div>

          <div className="seguimiento-detail-grid">
            {/* Left: History Stepper */}
            <div className="seguimiento-card history-section">
              <h3 className="history-title">Historial de seguimiento</h3>
              <p className="history-subtitle">Trayectoria completa del pedido</p>
              
              <div className="history-stepper">
                {pedido.historial.map((step, index) => (
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
                  <span>{pedido.direccion}</span>
                </div>
              </div>

              {/* Conditional Rendering for Repartidor */}
              {pedido.estado === 'En tránsito' && (
                <div className="seguimiento-card info-card">
                  <span className="info-card-label">REPARTIDOR ASIGNADO</span>
                  <div className="info-card-content">
                    <div className="repartidor-avatar">
                      {pedido.repartidor.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
                    </div>
                    <div className="repartidor-details">
                      <span className="repartidor-name">{pedido.repartidor}</span>
                      <span className="repartidor-status">En ruta activa</span>
                    </div>
                  </div>
                </div>
              )}

              <div className="seguimiento-card info-card">
                <span className="info-card-label">DEPÓSITO DE ORIGEN</span>
                <div className="info-card-stack">
                  <div className="info-stack-item">
                    <Warehouse size={16} className="info-icon-navy" />
                    <span style={{ fontSize: '0.875rem', color: 'var(--color-slate-700)' }}>{pedido.deposito}</span>
                  </div>
                  <div className="info-stack-item">
                    <Store size={16} className="info-icon-navy" />
                    <span style={{ fontSize: '0.875rem', color: 'var(--color-slate-700)' }}>{pedido.comercio.nombre}</span>
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
