import { Building2, Navigation, Package, Mail, Lock, User, Phone, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import logo from '../assets/Logo.png';
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

      {/* ── TopBar (mismo estilo que InventarioTopBar) ── */}
      <header className="register-topbar">
        <div className="register-topbar-brand">
          <img src={logo} alt="LogiRed" className="register-topbar-logo" />
        </div>
        <p className="register-topbar-title">Crear una cuenta nueva</p>
      </header>

      {/* ── Contenido ── */}
      <main className="register-content">
        <div>
          <h1 className="register-header-title">Registrate en LogiRed</h1>
          <p className="register-header-subtitle">
            Completá tus datos para comenzar a operar en la red logística.
          </p>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>

          {/* ── Panel 1: Datos de acceso ── */}
          <div className="register-panel">
            <div className="register-panel-bar" />
            <h2 className="register-panel-title">1. Datos de acceso</h2>
            <div className="register-fields-group">

              <div className="register-field">
                <label className="register-label">Correo Electrónico</label>
                <div className="register-input-wrapper">
                  <Mail className="register-input-icon" size={16} />
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

              <div className="register-grid-2">
                <div className="register-field">
                  <label className="register-label">Contraseña</label>
                  <div className="register-input-wrapper">
                    <Lock className="register-input-icon" size={16} />
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
                    <Lock className="register-input-icon" size={16} />
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

          {/* ── Panel 2: Selección de perfil ── */}
          <div className="register-panel">
            <div className="register-panel-bar" />
            <h2 className="register-panel-title">2. Seleccioná tu perfil</h2>
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

          {/* ── Panel 3: Datos específicos por rol ── */}
          {userType && (
            <div className="register-panel">
              <div className="register-panel-bar" />
              <h2 className="register-panel-title">
                3. Datos de {activeRole?.title}
              </h2>

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

          {/* ── Panel submit ── */}
          <div className="register-panel">
            <button
              type="submit"
              disabled={!userType || loading}
              className="register-submit-btn"
            >
              {loading
                ? 'Creando cuenta...'
                : `Crear cuenta${activeRole ? ` como ${activeRole.title}` : ''}`}
            </button>
          </div>
        </form>

        <p className="register-footer">
          ¿Ya tenés una cuenta?{' '}
          <Link to="/">Iniciá sesión aquí</Link>
        </p>
      </main>
    </div>
  );
}
