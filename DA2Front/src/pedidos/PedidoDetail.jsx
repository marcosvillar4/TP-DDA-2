import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, UserPlus, ChevronDown } from 'lucide-react';
import { pedidosMock } from './mockData';
import BadgeEstado from './components/BadgeEstado';
import StepperHistorial from './components/StepperHistorial';
import './styles/PedidoDetail.css';

const estadosConfig = [
  { name: 'Pendiente', color: 'var(--color-amber-dark)' },
  { name: 'Preparando', color: 'var(--color-blue)' },
  { name: 'En tránsito', color: 'var(--color-violet)' },
  { name: 'Entregado', color: 'var(--color-green)' },
  { name: 'Cancelado', color: 'var(--color-red)' },
];

export default function PedidoDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const pedido = pedidosMock.find(p => p.id === id) || pedidosMock[0];

  const [isEstadoOpen, setIsEstadoOpen] = useState(false);
  const [selectedEstado, setSelectedEstado] = useState(pedido.estado);
  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsEstadoOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [dropdownRef]);

  return (
    <div className="pedido-detail-page">
      <button className="pedido-btn-volver" onClick={() => navigate('/pedidos')}>
        <ArrowLeft size={16} /> Volver a Pedidos
      </button>

      <div className="pedido-detail-header">
        <div className="pedido-detail-title-group">
          <h1 className="pedido-detail-title">{pedido.id}</h1>
          <BadgeEstado estado={selectedEstado} />
        </div>
        <div className="pedido-detail-actions">
          
          <div className="custom-dropdown" ref={dropdownRef}>
            <button 
              className="custom-dropdown-trigger" 
              onClick={() => setIsEstadoOpen(!isEstadoOpen)}
            >
              Actualizar estado <ChevronDown size={16} />
            </button>
            {isEstadoOpen && (
              <div className="custom-dropdown-menu">
                {estadosConfig.map(e => (
                  <button 
                    key={e.name} 
                    className="custom-dropdown-item" 
                    onClick={() => {
                      setSelectedEstado(e.name);
                      setIsEstadoOpen(false);
                    }}
                  >
                    <span className="dropdown-dot" style={{ backgroundColor: e.color }}></span>
                    {e.name}
                  </button>
                ))}
              </div>
            )}
          </div>
          {userRole === 'ADMIN' && (
            <button className="pedido-btn-asignar">
              <UserPlus size={16} /> Asignar repartidor
            </button>
          )}
        </div>
      </div>

      <div className="pedido-detail-grid">
        <div className="pedido-main-col">
          
          <div className="pedido-card">
            <h3 className="pedido-card-title">Información del pedido</h3>
            <div className="pedido-info-grid">
              <div className="info-block">
                <span className="info-label">COMERCIO DE ORIGEN</span>
                <span className="info-value">{pedido.comercio.nombre}</span>
              </div>
              <div className="info-block">
                <span className="info-label">DESTINATARIO</span>
                <span className="info-value">{pedido.destinatario}</span>
              </div>
              <div className="info-block">
                <span className="info-label">DIRECCIÓN DE ENTREGA</span>
                <span className="info-value">{pedido.direccion}</span>
              </div>
              <div className="info-block">
                <span className="info-label">DEPÓSITO ASIGNADO</span>
                <span className="info-value">{pedido.deposito}</span>
              </div>
              <div className="info-block">
                <span className="info-label">REPARTIDOR</span>
                <span className="info-value">{pedido.repartidor}</span>
              </div>
              <div className="info-block">
                <span className="info-label">FECHA DE CREACIÓN</span>
                <span className="info-value">{pedido.fecha}</span>
              </div>
            </div>
          </div>

          <div className="pedido-card">
            <div className="pedido-card-header">
              <h3 className="pedido-card-title">Productos</h3>
              <span className="pedido-card-subtitle">{pedido.productos.reduce((acc, p) => acc + p.cantidad, 0)} unidades</span>
            </div>
            <table className="pedido-productos-table">
              <thead>
                <tr>
                  <th>PRODUCTO</th>
                  <th style={{textAlign: 'center'}}>CANT.</th>
                  <th style={{textAlign: 'right'}}>PESO UNIT.</th>
                </tr>
              </thead>
              <tbody>
                {pedido.productos.map((prod, idx) => (
                  <tr key={idx}>
                    <td>{prod.nombre}</td>
                    <td style={{textAlign: 'center'}}>{prod.cantidad}</td>
                    <td style={{textAlign: 'right'}}>{prod.pesoUnitario}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {pedido.observaciones && (
            <div className="pedido-card">
              <h3 className="pedido-card-title">Observaciones</h3>
              <p className="pedido-observaciones">{pedido.observaciones}</p>
            </div>
          )}

        </div>

        <div className="pedido-side-col">
          <div className="pedido-card border-top-violet">
            <span className="info-label mb-2">ESTADO ACTUAL</span>
            <BadgeEstado estado={selectedEstado} />
            <p className="info-last-event mt-3">
              Último evento: {pedido.historial.find(h => h.status === 'current')?.fecha || pedido.fecha}
            </p>
          </div>

          <StepperHistorial historial={pedido.historial} />
        </div>
      </div>
    </div>
  );
}
