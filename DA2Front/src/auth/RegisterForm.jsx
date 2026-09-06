import { Building2, Navigation, Package, Mail, Lock, User, Phone, MapPin, Truck, ShieldCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useRegister } from '../../hooks/useRegister';
import './styles/RegisterForm.css';

const roles = [
  {
    id: 'COMERCIO',
    title: 'Comercio',
    icon: <Building2 size={22} />,
    description: 'Gestiona pedidos e inventario para tus envíos.',
  },
  {
    id: 'REPARTIDOR',
    title: 'Repartidor',
    icon: <Navigation size={22} />,
    description: 'Toma pedidos y realiza la entrega de última milla.',
  },
  {
    id: 'DEPOSITO',
    title: 'Depósito',
    icon: <Package size={22} />,
    description: 'Administra mercadería física y prepara paquetes.',
  },
];

export default function RegisterForm() {
  const {
    baseData,
    userType,
    loading,
    setUserType,
    setExtraData,
    handleBaseChange,
    handleExtraChange,
    handleSubmit,
  } = useRegister();

  const activeRole = roles.find((r) => r.id === userType);

  return (
    <div className="register-page">

      {/* Panel izquierdo — Branding */}
      <div className="register-branding">
        <div>
          <div className="register-brand-logo">
            <Truck className="register-brand-logo-icon" />
            <span className="register-brand-logo-text">LogiRed</span>
          </div>
          <h1 className="register-brand-heading">
            Únete a la red logística del futuro.
          </h1>
          <p className="register-brand-subtitle">
            Crea tu cuenta hoy y comienza a operar. Conectamos comercios,
            depósitos y repartidores en una única plataforma inteligente.
          </p>
        </div>
        <div className="register-trust-card">
          <div className="register-trust-card-header">
            <ShieldCheck className="register-trust-icon" />
            <span className="register-trust-title">Plataforma Segura</span>
          </div>
          <p className="register-trust-desc">
            Tus datos están protegidos con encriptación de extremo a extremo
            y autenticación JWT.
          </p>
        </div>
      </div>

      {/* Panel derecho — Formulario */}
      <div className="register-form-panel">
        <div className="register-form-container">

          {/* Logo móvil */}
          <div className="register-mobile-logo">
            <Truck className="register-mobile-logo-icon" />
            <span className="register-mobile-logo-text">LogiRed</span>
          </div>

          <h2 className="register-heading">Crear una cuenta</h2>
          <p className="register-subheading">Completá tus datos para comenzar a operar.</p>

          <form onSubmit={handleSubmit} className="register-form">

            {/* ── 1. Datos de acceso ── */}
            <div>
              <h3 className="register-section-title">1. Datos de acceso</h3>
              <div className="register-fields-group">

                {/* Email */}
                <div className="register-field">
                  <label className="register-label">Correo Electrónico</label>
                  <div className="register-input-wrapper">
                    <Mail className="register-input-icon" size={18} />
                    <input
                      type="email"
                      name="email"
                      required
                      className="register-input has-icon"
                      placeholder="tu@correo.com"
                      value={baseData.email}
                      onChange={handleBaseChange}
                    />
                  </div>
                </div>

                {/* Contraseñas */}
                <div className="register-grid-2">
                  <div className="register-field">
                    <label className="register-label">Contraseña</label>
                    <div className="register-input-wrapper">
                      <Lock className="register-input-icon" size={18} />
                      <input
                        type="password"
                        name="password"
                        required
                        className="register-input has-icon"
                        placeholder="••••••••"
                        value={baseData.password}
                        onChange={handleBaseChange}
                      />
                    </div>
                  </div>
                  <div className="register-field">
                    <label className="register-label">Confirmar Contraseña</label>
                    <div className="register-input-wrapper">
                      <Lock className="register-input-icon" size={18} />
                      <input
                        type="password"
                        name="confirmPassword"
                        required
                        className="register-input has-icon"
                        placeholder="••••••••"
                        value={baseData.confirmPassword}
                        onChange={handleBaseChange}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <hr className="register-divider" />

            {/* ── 2. Selección de perfil ── */}
            <div>
              <h3 className="register-section-title">2. Seleccioná tu perfil</h3>
              <div className="register-role-cards">
                {roles.map((role) => (
                  <button
                    key={role.id}
                    type="button"
                    onClick={() => {
                      setUserType(role.id);
                      setExtraData({});
                    }}
                    className={`register-role-card${userType === role.id ? ' active' : ''}`}
                  >
                    <div className="register-role-icon">{role.icon}</div>
                    <div className="register-role-title">{role.title}</div>
                    <div className="register-role-desc">{role.description}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* ── 3. Datos específicos por rol ── */}
            {userType && (
              <div className="register-extra-section">
                <div className="register-extra-title">
                  3. Datos de {activeRole?.title}
                </div>

                {/* COMERCIO */}
                {userType === 'COMERCIO' && (
                  <div className="register-fields-group">
                    <div className="register-grid-2">
                      <div className="register-field">
                        <label className="register-label">Razón Social / Nombre</label>
                        <input type="text" name="razonSocial" required className="register-input" placeholder="Ej: Urban Shoes S.A." onChange={handleExtraChange} />
                      </div>
                      <div className="register-field">
                        <label className="register-label">CUIT</label>
                        <input type="text" name="cuit" required className="register-input" placeholder="30-XXXXXXXX-X" onChange={handleExtraChange} />
                      </div>
                    </div>
                    <div className="register-field">
                      <label className="register-label">Dirección de Retiro</label>
                      <div className="register-input-wrapper">
                        <MapPin className="register-input-icon" size={16} />
                        <input type="text" name="direccion" required className="register-input has-icon" placeholder="Av. Principal 123, Ciudad" onChange={handleExtraChange} />
                      </div>
                    </div>
                    <div className="register-field">
                      <label className="register-label">Teléfono de Contacto</label>
                      <div className="register-input-wrapper">
                        <Phone className="register-input-icon" size={16} />
                        <input type="tel" name="telefono" required className="register-input has-icon" placeholder="+54 11 XXXX-XXXX" onChange={handleExtraChange} />
                      </div>
                    </div>
                  </div>
                )}

                {/* REPARTIDOR */}
                {userType === 'REPARTIDOR' && (
                  <div className="register-fields-group">
                    <div className="register-grid-2">
                      <div className="register-field">
                        <label className="register-label">Nombre</label>
                        <div className="register-input-wrapper">
                          <User className="register-input-icon" size={16} />
                          <input type="text" name="nombre" required className="register-input has-icon" placeholder="Carlos" onChange={handleExtraChange} />
                        </div>
                      </div>
                      <div className="register-field">
                        <label className="register-label">Apellido</label>
                        <input type="text" name="apellido" required className="register-input" placeholder="Ruiz" onChange={handleExtraChange} />
                      </div>
                    </div>
                    <div className="register-grid-2">
                      <div className="register-field">
                        <label className="register-label">DNI</label>
                        <input type="text" name="dni" required className="register-input" placeholder="XX.XXX.XXX" onChange={handleExtraChange} />
                      </div>
                      <div className="register-field">
                        <label className="register-label">Tipo de Vehículo</label>
                        <select name="vehiculo" required className="register-select" onChange={handleExtraChange} defaultValue="">
                          <option value="" disabled>Seleccione...</option>
                          <option value="MOTO">Moto</option>
                          <option value="AUTO">Auto / Utilitario</option>
                          <option value="BICI">Bicicleta</option>
                        </select>
                      </div>
                    </div>
                  </div>
                )}

                {/* DEPOSITO */}
                {userType === 'DEPOSITO' && (
                  <div className="register-fields-group">
                    <div className="register-field">
                      <label className="register-label">Nombre del Nodo / Sucursal</label>
                      <div className="register-input-wrapper">
                        <Building2 className="register-input-icon" size={16} />
                        <input type="text" name="nombreNodo" required className="register-input has-icon" placeholder="Depósito Palermo Central" onChange={handleExtraChange} />
                      </div>
                    </div>
                    <div className="register-grid-2">
                      <div className="register-field">
                        <label className="register-label">Ubicación / Zona</label>
                        <div className="register-input-wrapper">
                          <MapPin className="register-input-icon" size={16} />
                          <input type="text" name="zona" required className="register-input has-icon" placeholder="CABA - Norte" onChange={handleExtraChange} />
                        </div>
                      </div>
                      <div className="register-field">
                        <label className="register-label">Capacidad Máx. (Paquetes)</label>
                        <input type="number" name="capacidad" required min="1" className="register-input" placeholder="Ej: 5000" onChange={handleExtraChange} />
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* ── Botón submit ── */}
            <button
              type="submit"
              disabled={!userType || loading}
              className="register-submit-btn"
            >
              {loading
                ? 'Creando cuenta...'
                : `Crear cuenta como ${activeRole?.title ?? '...'}`}
            </button>
          </form>

          <p className="register-footer">
            ¿Ya tenés una cuenta?{' '}
            <Link to="/">Iniciá sesión aquí</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
