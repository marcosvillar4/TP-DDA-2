import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useAuthUser } from '../../hooks/useAuthUser';
import { useComercioForm } from '../../hooks/useComercioForm';
import BadgeComercio from './components/BadgeComercio';
import './styles/ComercioForm.css';

/**
 * Edición de un comercio. El alta no existe acá: los comercios se crean
 * con el registro (rol COMERCIO) y los valida un administrador.
 */
export default function ComercioForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = useAuthUser();

  const esComercio = user?.rol === 'COMERCIO';
  const rutaVolver = esComercio ? '/mi-comercio' : '/comercios';
  const textoVolver = esComercio ? 'Volver a Mi Comercio' : 'Volver a Comercios';

  const { form, estado, loading, saving, error, handleChange, handleSubmit } =
    useComercioForm(id, rutaVolver);

  if (loading) {
    return <div className="comercio-form-page"><p>Cargando comercio...</p></div>;
  }

  if (error) {
    return (
      <div className="comercio-form-page">
        <button className="comercio-btn-volver" onClick={() => navigate(rutaVolver)} type="button">
          <ArrowLeft size={16} /> {textoVolver}
        </button>
        <p>{error}</p>
      </div>
    );
  }

  return (
    <div className="comercio-form-page">
      <button className="comercio-btn-volver" onClick={() => navigate(rutaVolver)} type="button">
        <ArrowLeft size={16} /> {textoVolver}
      </button>

      <div className="comercio-form-header">
        <h1 className="comercio-form-title">Editar: {form.nombreComercial}</h1>
      </div>

      <div className="comercio-form-card">
        <h3 className="comercio-form-subtitle">Datos del comercio</h3>

        <form onSubmit={handleSubmit} className="comercio-form">
          <div className="form-grid">
            <div className="form-group">
              <label>NOMBRE COMERCIAL <span className="required">*</span></label>
              <input
                type="text"
                name="nombreComercial"
                placeholder="Ej: Urban Shoes"
                value={form.nombreComercial}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>RAZÓN SOCIAL <span className="required">*</span></label>
              <input
                type="text"
                name="razonSocial"
                placeholder="Ej: Urban Shoes S.A."
                value={form.razonSocial}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>CUIT <span className="required">*</span></label>
              <input
                type="text"
                name="cuit"
                placeholder="30-XXXXXXX-X"
                value={form.cuit}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>EMAIL <span className="required">*</span></label>
              <input
                type="email"
                name="email"
                placeholder="ventas@comercio.com.ar"
                value={form.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>TELÉFONO <span className="required">*</span></label>
              <input
                type="text"
                name="telefono"
                placeholder="+54 11 XXXX-XXXX"
                value={form.telefono}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group form-group-full">
            <label>DIRECCIÓN <span className="required">*</span></label>
            <input
              type="text"
              name="direccion"
              placeholder="Av. Corrientes 1234, CABA"
              value={form.direccion}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group form-group-full">
            <label>ESTADO DE LA CUENTA</label>
            <div>
              <BadgeComercio estado={estado} />
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn-cancel" onClick={() => navigate(rutaVolver)}>
              Cancelar
            </button>
            <button type="submit" className="btn-submit" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar comercio'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
