import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { comerciosMock } from './mockData';
import './styles/ComercioForm.css';

export default function ComercioForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [formData, setFormData] = useState({
    nombre: '',
    cuit: '',
    responsable: '',
    email: '',
    telefono: '',
    direccion: '',
    estado: 'Activo'
  });

  useEffect(() => {
    if (isEditMode) {
      const comercio = comerciosMock.find(c => c.id === id);
      if (comercio) {
        setFormData({
          nombre: comercio.nombre || '',
          cuit: comercio.cuit || '',
          responsable: comercio.responsable || '',
          email: comercio.email || '',
          telefono: comercio.telefono || '',
          direccion: comercio.direccion || '',
          estado: comercio.estado || 'Activo'
        });
      }
    }
  }, [id, isEditMode]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Simulate save and go back
    navigate('/comercios');
  };

  const title = isEditMode ? `Editar: ${formData.nombre}` : 'Nuevo comercio';

  return (
    <div className="comercio-form-page">
      <button className="comercio-btn-volver" onClick={() => navigate('/comercios')} type="button">
        <ArrowLeft size={16} /> Volver a Comercios
      </button>

      <div className="comercio-form-header">
        <h1 className="comercio-form-title">{title}</h1>
      </div>

      <div className="comercio-form-card">
        <h3 className="comercio-form-subtitle">Datos del comercio</h3>
        
        <form onSubmit={handleSubmit} className="comercio-form">
          <div className="form-group form-group-full">
            <label>NOMBRE / RAZÓN SOCIAL <span className="required">*</span></label>
            <input 
              type="text" 
              name="nombre"
              placeholder="Ej: Urban Shoes S.A." 
              value={formData.nombre}
              onChange={handleChange}
              required 
            />
          </div>

          <div className="form-grid">
            <div className="form-group">
              <label>CUIT <span className="required">*</span></label>
              <input 
                type="text" 
                name="cuit"
                placeholder="30-XXXXXXX-X" 
                value={formData.cuit}
                onChange={handleChange}
                required 
              />
            </div>
            
            <div className="form-group">
              <label>CONTACTO PRINCIPAL</label>
              <input 
                type="text" 
                name="responsable"
                placeholder="Nombre del responsable" 
                value={formData.responsable}
                onChange={handleChange}
              />
            </div>
            
            <div className="form-group">
              <label>EMAIL <span className="required">*</span></label>
              <input 
                type="email" 
                name="email"
                placeholder="ventas@comercio.com.ar" 
                value={formData.email}
                onChange={handleChange}
                required 
              />
            </div>
            
            <div className="form-group">
              <label>TELÉFONO</label>
              <input 
                type="text" 
                name="telefono"
                placeholder="+54 11 XXXX-XXXX" 
                value={formData.telefono}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-group form-group-full">
            <label>DIRECCIÓN</label>
            <input 
              type="text" 
              name="direccion"
              placeholder="Av. Corrientes 1234, CABA" 
              value={formData.direccion}
              onChange={handleChange}
            />
          </div>

          <div className="form-group form-group-full">
            <label>ESTADO</label>
            <div className="radio-group">
              <label className="radio-label">
                <input 
                  type="radio" 
                  name="estado" 
                  value="Activo" 
                  checked={formData.estado === 'Activo'}
                  onChange={handleChange}
                />
                <span className="radio-custom"></span> Activo
              </label>
              <label className="radio-label">
                <input 
                  type="radio" 
                  name="estado" 
                  value="Inactivo" 
                  checked={formData.estado === 'Inactivo'}
                  onChange={handleChange}
                />
                <span className="radio-custom"></span> Inactivo
              </label>
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn-cancel" onClick={() => navigate('/comercios')}>
              Cancelar
            </button>
            <button type="submit" className="btn-submit">
              Guardar comercio
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
