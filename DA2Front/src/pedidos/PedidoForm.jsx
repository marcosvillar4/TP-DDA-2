import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import { getComercios } from '../../api/inventarioApi';
import { crearPedido } from '../../api/pedidosApi';
import Swal from 'sweetalert2';

export default function PedidoForm() {
  const navigate = useNavigate();
  const [comercios, setComercios] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    comercioId: '',
    direccionOrigen: '',
    direccionDestino: ''
  });

  useEffect(() => {
    getComercios()
      .then(data => {
        setComercios(data);
        if (data.length > 0) {
          setFormData(prev => ({ ...prev, comercioId: data[0].id }));
        }
      })
      .catch(err => console.error("Error loading comercios:", err));
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await crearPedido({
        comercioId: parseInt(formData.comercioId),
        direccionOrigen: formData.direccionOrigen,
        direccionDestino: formData.direccionDestino
      });
      Swal.fire('Éxito', 'Pedido creado exitosamente', 'success');
      navigate('/pedidos');
    } catch (error) {
      Swal.fire('Error', error.message || 'Error al crear el pedido', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '1.5rem', maxWidth: '800px', margin: '0 auto' }}>
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
          <div style={{ display: 'grid', gap: '1.5rem', marginBottom: '2rem' }}>
            
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
                  border: '1px solid var(--color-slate-300)',
                  outline: 'none'
                }}
                required
              >
                <option value="" disabled>
                  {comercios.length === 0 ? "No hay comercios registrados" : "Seleccione un comercio..."}
                </option>
                {comercios.map(c => (
                  <option key={c.id} value={c.id}>{c.nombreComercial}</option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: 'var(--color-slate-700)' }}>
                Dirección de Origen
              </label>
              <input 
                type="text" 
                name="direccionOrigen"
                value={formData.direccionOrigen}
                onChange={handleChange}
                placeholder="Ej. Av. Corrientes 1234, CABA (Opcional, si no se carga usa la del comercio)"
                style={{
                  padding: '0.625rem', borderRadius: '0.375rem', 
                  border: '1px solid var(--color-slate-300)',
                  outline: 'none'
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
                  border: '1px solid var(--color-slate-300)',
                  outline: 'none'
                }}
                required
              />
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
