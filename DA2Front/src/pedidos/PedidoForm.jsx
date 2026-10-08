import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Plus, Trash2 } from 'lucide-react';
import { getComercios } from '../../api/inventarioApi';
import { getDepositosPorComercio, getItemsInventarioPorDeposito } from '../../api/depositosApi';
import { crearPedido } from '../../api/pedidosApi';
import Swal from 'sweetalert2';

export default function PedidoForm() {
  const navigate = useNavigate();
  const [comercios, setComercios] = useState([]);
  const [depositos, setDepositos] = useState([]);
  const [itemsInventario, setItemsInventario] = useState([]);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    comercioId: '',
    depositoId: '',
    direccionOrigen: '',
    direccionDestino: ''
  });

  const [detalles, setDetalles] = useState([]);

  useEffect(() => {
    getComercios()
      .then(data => {
        setComercios(data);
        if (data.length > 0) {
          handleComercioChange(data[0].id);
        }
      })
      .catch(err => console.error("Error cargando comercios:", err));
  }, []);

  const handleComercioChange = async (comercioId) => {
    setFormData(prev => ({ ...prev, comercioId, depositoId: '' }));
    setDepositos([]);
    setItemsInventario([]);
    setDetalles([]);

    try {
      const deps = await getDepositosPorComercio(comercioId);
      setDepositos(deps);
      if (deps.length > 0) {
        handleDepositoChange(deps[0].id);
      }
    } catch (err) {
      console.error("Error cargando depositos:", err);
    }
  };

  const handleDepositoChange = async (depositoId) => {
    setFormData(prev => ({ ...prev, depositoId }));
    setItemsInventario([]);
    setDetalles([]);

    if (!depositoId) return;

    try {
      const items = await getItemsInventarioPorDeposito(depositoId);
      setItemsInventario(items);
    } catch (err) {
      console.error("Error cargando items del deposito:", err);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'comercioId') {
      handleComercioChange(value);
    } else if (name === 'depositoId') {
      handleDepositoChange(value);
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const agregarProducto = (item) => {
    const existe = detalles.find(d => d.productoId === item.productoId);
    if (existe) {
      if (existe.cantidad >= item.cantidad) {
        Swal.fire('Atención', 'No hay más stock disponible de este producto', 'warning');
        return;
      }
      setDetalles(detalles.map(d => 
        d.productoId === item.productoId ? { ...d, cantidad: d.cantidad + 1 } : d
      ));
    } else {
      if (item.cantidad <= 0) {
        Swal.fire('Atención', 'Este producto no tiene stock', 'warning');
        return;
      }
      setDetalles([...detalles, { productoId: item.productoId, nombre: item.productoNombre, cantidad: 1, maxStock: item.cantidad }]);
    }
  };

  const quitarProducto = (productoId) => {
    setDetalles(detalles.filter(d => d.productoId !== productoId));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (detalles.length === 0) {
      Swal.fire('Error', 'Debe agregar al menos un producto al pedido', 'error');
      return;
    }
    if (!formData.depositoId) {
      Swal.fire('Error', 'Debe seleccionar un depósito de origen', 'error');
      return;
    }

    setLoading(true);
    try {
      await crearPedido({
        comercioId: parseInt(formData.comercioId),
        depositoId: parseInt(formData.depositoId),
        direccionOrigen: formData.direccionOrigen,
        direccionDestino: formData.direccionDestino,
        detalles: detalles.map(d => ({ productoId: d.productoId, cantidad: d.cantidad }))
      });
      Swal.fire('¡Éxito!', 'Pedido creado y stock descontado', 'success');
      navigate('/pedidos');
    } catch (error) {
      Swal.fire('Error', error.message || 'Error al crear el pedido', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '1.5rem', maxWidth: '900px', margin: '0 auto' }}>
      <button 
        style={{
          background: 'none', border: 'none', color: 'var(--color-navy)',
          display: 'flex', alignItems: 'center', gap: '0.5rem',
          cursor: 'pointer', marginBottom: '1.5rem', fontWeight: '500'
        }} 
        onClick={() => navigate('/pedidos')}
      >
        <ArrowLeft size={16} /> Volver a Pedidos
      </button>

      <div style={{ backgroundColor: '#fff', borderRadius: '0.75rem', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ marginTop: 0, marginBottom: '1.5rem', color: 'var(--color-slate-800)' }}>Nuevo Pedido</h2>
        
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: 'var(--color-slate-700)' }}>
                Comercio
              </label>
              <select 
                name="comercioId" 
                value={formData.comercioId} 
                onChange={handleChange}
                style={{
                  padding: '0.625rem', borderRadius: '0.375rem', 
                  border: '1px solid var(--color-slate-300)', outline: 'none'
                }}
                required
              >
                <option value="" disabled>Seleccione un comercio...</option>
                {comercios.map(c => (
                  <option key={c.id} value={c.id}>{c.nombreComercial}</option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: 'var(--color-slate-700)' }}>
                Depósito de Origen
              </label>
              <select 
                name="depositoId" 
                value={formData.depositoId} 
                onChange={handleChange}
                style={{
                  padding: '0.625rem', borderRadius: '0.375rem', 
                  border: '1px solid var(--color-slate-300)', outline: 'none'
                }}
                required
              >
                <option value="" disabled>Seleccione un depósito...</option>
                {depositos.map(d => (
                  <option key={d.id} value={d.id}>{d.nombreDeposito} ({d.direccion})</option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: 'var(--color-slate-700)' }}>
                Dirección de Origen (Opcional)
              </label>
              <input 
                type="text" 
                name="direccionOrigen"
                value={formData.direccionOrigen}
                onChange={handleChange}
                placeholder="Si se deja vacío usa la del depósito"
                style={{
                  padding: '0.625rem', borderRadius: '0.375rem', 
                  border: '1px solid var(--color-slate-300)', outline: 'none'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: 'var(--color-slate-700)' }}>
                Dirección de Destino
              </label>
              <input 
                type="text" 
                name="direccionDestino"
                value={formData.direccionDestino}
                onChange={handleChange}
                placeholder="Ej. Av. Cabildo 500, CABA"
                style={{
                  padding: '0.625rem', borderRadius: '0.375rem', 
                  border: '1px solid var(--color-slate-300)', outline: 'none'
                }}
                required
              />
            </div>
          </div>

          <hr style={{ border: 'none', borderTop: '1px solid var(--color-slate-200)', margin: '2rem 0' }} />

          <h3 style={{ marginTop: 0, marginBottom: '1rem', color: 'var(--color-slate-800)', fontSize: '1.1rem' }}>Productos del Pedido</h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
            {/* Lista de productos disponibles */}
            <div style={{ border: '1px solid var(--color-slate-200)', borderRadius: '0.5rem', padding: '1rem' }}>
              <h4 style={{ margin: '0 0 1rem 0', color: 'var(--color-slate-600)' }}>Inventario Disponible</h4>
              {itemsInventario.length === 0 ? (
                <p style={{ fontSize: '0.875rem', color: 'var(--color-slate-500)' }}>No hay productos en este depósito.</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {itemsInventario.map(item => (
                    <li key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem', backgroundColor: 'var(--color-slate-50)', borderRadius: '0.375rem' }}>
                      <div>
                        <span style={{ display: 'block', fontWeight: '500', fontSize: '0.875rem' }}>{item.productoNombre}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>Stock: {item.cantidad}</span>
                      </div>
                      <button 
                        type="button" 
                        onClick={() => agregarProducto(item)}
                        style={{ background: 'var(--color-navy)', color: 'white', border: 'none', borderRadius: '0.25rem', padding: '0.25rem 0.5rem', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem' }}
                      >
                        <Plus size={14} /> Agregar
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {/* Carrito */}
            <div style={{ border: '1px solid var(--color-slate-200)', borderRadius: '0.5rem', padding: '1rem', backgroundColor: 'var(--color-slate-50)' }}>
              <h4 style={{ margin: '0 0 1rem 0', color: 'var(--color-slate-600)' }}>Detalle a enviar</h4>
              {detalles.length === 0 ? (
                <p style={{ fontSize: '0.875rem', color: 'var(--color-slate-500)' }}>Aún no agregaste productos.</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {detalles.map(d => (
                    <li key={d.productoId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem', backgroundColor: 'white', borderRadius: '0.375rem', border: '1px solid var(--color-slate-200)' }}>
                      <div>
                        <span style={{ display: 'block', fontWeight: '500', fontSize: '0.875rem' }}>{d.nombre}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>Cant: {d.cantidad}</span>
                      </div>
                      <button 
                        type="button" 
                        onClick={() => quitarProducto(d.productoId)}
                        style={{ background: 'none', color: 'var(--color-red)', border: 'none', cursor: 'pointer', padding: '0.25rem' }}
                      >
                        <Trash2 size={16} />
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
            <button 
              type="button" 
              onClick={() => navigate('/pedidos')}
              style={{
                padding: '0.5rem 1rem', borderRadius: '0.375rem',
                border: '1px solid var(--color-slate-300)',
                background: 'white', color: 'var(--color-slate-600)',
                cursor: 'pointer'
              }}
            >
              Cancelar
            </button>
            <button 
              type="submit" 
              disabled={loading}
              style={{
                padding: '0.5rem 1rem', borderRadius: '0.375rem',
                border: 'none', background: 'var(--color-navy)', 
                color: 'white', cursor: loading ? 'not-allowed' : 'pointer',
                display: 'flex', alignItems: 'center', gap: '0.5rem'
              }}
            >
              <Save size={16} /> {loading ? 'Guardando...' : 'Crear Pedido'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
