import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, UserPlus, Truck, CheckCircle, XCircle } from 'lucide-react';
import { getPedidoById, cancelarPedido, iniciarViajePedido, entregarPedido } from '../../api/pedidosApi';
import { getComercios } from '../../api/inventarioApi';
import BadgeEstado from './components/BadgeEstado';
import StepperHistorial from './components/StepperHistorial';
import './styles/PedidoDetail.css';
import Swal from 'sweetalert2';

export default function PedidoDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [pedido, setPedido] = useState(null);
  const [comercioNombre, setComercioNombre] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchPedido = () => {
    Promise.all([getPedidoById(id), getComercios()])
      .then(([pedidoData, comerciosData]) => {
        setPedido(pedidoData);
        const comercio = comerciosData.find(c => c.id === pedidoData.comercioId);
        setComercioNombre(comercio ? comercio.nombreComercial : `Comercio ${pedidoData.comercioId}`);
      })
      .catch(err => {
        console.error(err);
        Swal.fire('Error', 'No se pudo cargar el pedido', 'error');
        navigate('/pedidos');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchPedido();
  }, [id]);

  const handleAction = async (actionFn, actionName) => {
    try {
      const result = await Swal.fire({
        title: `¿Confirmar ${actionName}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Sí, confirmar',
        cancelButtonText: 'Cancelar'
      });
      if (result.isConfirmed) {
        await actionFn(id);
        Swal.fire('Éxito', `El pedido ha sido actualizado`, 'success');
        fetchPedido();
      }
    } catch (err) {
      Swal.fire('Error', err.message || 'Error al procesar la acción', 'error');
    }
  };

  const handleAsignarRepartidor = async () => {
    try {
      const { getRepartidores } = await import('../../api/repartidoresApi');
      const { asignarRepartidor } = await import('../../api/pedidosApi');
      
      const repartidores = await getRepartidores();
      const options = {};
      repartidores.forEach(r => {
        options[r.id] = `${r.nombreCompleto} (${r.estado})`;
      });

      const { value: repartidorId } = await Swal.fire({
        title: 'Asignar Repartidor',
        input: 'select',
        inputOptions: options,
        inputPlaceholder: 'Seleccione un repartidor',
        showCancelButton: true,
        confirmButtonText: 'Asignar',
        cancelButtonText: 'Cancelar',
        inputValidator: (value) => {
          if (!value) return 'Debes seleccionar un repartidor';
        }
      });

      if (repartidorId) {
        await asignarRepartidor(id, repartidorId);
        Swal.fire('Éxito', 'Repartidor asignado', 'success');
        fetchPedido();
      }
    } catch (err) {
      Swal.fire('Error', err.message || 'Error al asignar', 'error');
    }
  };

  if (loading) return <div style={{padding: '2rem'}}>Cargando pedido...</div>;
  if (!pedido) return null;

  const historialUI = (pedido.historial || []).map((h, i) => {
    const isCurrent = i === 0;
    return {
      estado: h.estado,
      fecha: new Date(h.fechaHora).toLocaleString(),
      ubicacion: 'Sistema',
      status: isCurrent ? 'current' : 'completed'
    };
  }).reverse();

  return (
    <div className="pedido-detail-page">
      <button className="pedido-btn-volver" onClick={() => navigate('/pedidos')}>
        <ArrowLeft size={16} /> Volver a Pedidos
      </button>

      <div className="pedido-detail-header">
        <div className="pedido-detail-title-group">
          <h1 className="pedido-detail-title">LOG-{pedido.id}</h1>
          <BadgeEstado estado={pedido.estado} />
        </div>
        <div className="pedido-detail-actions" style={{display: 'flex', gap: '0.5rem'}}>
          {pedido.estado === 'PENDIENTE_COTIZACION' && (
            <button className="pedido-btn-asignar" onClick={handleAsignarRepartidor}>
              <UserPlus size={16} /> Asignar repartidor
            </button>
          )}
          {pedido.estado === 'ASIGNADO' && (
            <button className="pedido-btn-asignar" onClick={() => handleAction(iniciarViajePedido, 'Iniciar Viaje')} style={{backgroundColor: 'var(--color-blue)', color: 'white', border: 'none'}}>
              <Truck size={16} /> Iniciar Viaje
            </button>
          )}
          {pedido.estado === 'EN_CAMINO' && (
            <button className="pedido-btn-asignar" onClick={() => handleAction(entregarPedido, 'Entrega')} style={{backgroundColor: 'var(--color-green)', color: 'white', border: 'none'}}>
              <CheckCircle size={16} /> Marcar Entregado
            </button>
          )}
          {['PENDIENTE_COTIZACION', 'ASIGNADO', 'EN_CAMINO'].includes(pedido.estado) && (
            <button className="pedido-btn-asignar" onClick={() => handleAction(cancelarPedido, 'Cancelación')} style={{borderColor: 'var(--color-red)', color: 'var(--color-red)'}}>
              <XCircle size={16} /> Cancelar
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
                <span className="info-label">COMERCIO</span>
                <span className="info-value">{comercioNombre}</span>
              </div>
              <div className="info-block">
                <span className="info-label">DIRECCIÓN DE ORIGEN</span>
                <span className="info-value">{pedido.direccionOrigen || "No especificada"}</span>
              </div>
              <div className="info-block">
                <span className="info-label">DIRECCIÓN DE DESTINO</span>
                <span className="info-value">{pedido.direccionDestino}</span>
              </div>
              <div className="info-block">
                <span className="info-label">FECHA DE CREACIÓN</span>
                <span className="info-value">{pedido.fechaCreacion ? new Date(pedido.fechaCreacion).toLocaleString() : "-"}</span>
              </div>
              <div className="info-block">
                <span className="info-label">REPARTIDOR</span>
                <span className="info-value">{pedido.repartidorNombre || "Sin asignar"}</span>
              </div>
            </div>
          </div>

        </div>

        <div className="pedido-side-col">
          <div className="pedido-card border-top-violet">
            <span className="info-label mb-2">ESTADO ACTUAL</span>
            <BadgeEstado estado={pedido.estado} />
          </div>

          <StepperHistorial historial={historialUI} />
        </div>
      </div>
    </div>
  );
}
