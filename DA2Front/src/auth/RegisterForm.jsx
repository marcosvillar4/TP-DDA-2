import { Building2, Navigation, Mail, User, Phone, MapPin, Hash } from "lucide-react";
import { Link } from "react-router-dom";
import { useRegister } from "../../hooks/useRegister";
import { TextField } from "./components/TextField";
import { PasswordField } from "./components/PasswordField";
import { Button } from "./components/Button";
import { AuthLayout } from "./AuthLayout";
import "./styles/RegisterForm.css";

const roles = [
  {
    id: "COMERCIO",
    title: "Comercio",
    icon: <Building2 size={22} />,
    description: "Gestiona pedidos e inventario para tus envíos.",
  },
  {
    id: "REPARTIDOR",
    title: "Repartidor",
    icon: <Navigation size={22} />,
    description: "Toma pedidos y realiza la entrega de última milla.",
  },
];

export default function RegisterForm() {
  const {
    baseData,
    extraData,
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
      <header className="register-topbar">
        <span className="register-brand-mark">
          Logi<span className="text-red">Red</span>
        </span>
      </header>

      <main className="register-content">
        
        <div>
          <h1 className="register-header-title">Registrate en LogiRed</h1>
          <p className="register-header-subtitle">
            Completá tus datos para comenzar a operar en la red logística.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="register-form">
          {/* Panel 1: datos de acceso */}
          <section className="register-panel">
            <div className="register-panel-bar" />
            <h2 className="register-panel-title">1. Datos de acceso</h2>
            <div className="register-fields-group">
              <TextField
                id="email"
                name="email"
                type="email"
                label="Correo electrónico"
                icon={Mail}
                required
                placeholder="tu@correo.com"
                autoComplete="username"
                value={baseData.email}
                onChange={handleBaseChange}
              />
              <div className="register-grid-2">
                <PasswordField
                  id="password"
                  name="password"
                  label="Contraseña"
                  required
                  minLength={8}
                  placeholder="••••••••"
                  autoComplete="new-password"
                  value={baseData.password}
                  onChange={handleBaseChange}
                />
                <PasswordField
                  id="confirmPassword"
                  name="confirmPassword"
                  label="Confirmar contraseña"
                  required
                  placeholder="••••••••"
                  autoComplete="new-password"
                  value={baseData.confirmPassword}
                  onChange={handleBaseChange}
                />
              </div>
            </div>
          </section>

          {/* Panel 2: selección de perfil */}
          <section className="register-panel">
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
                  className={`register-role-card${userType === role.id ? " active" : ""}`}
                >
                  <div className="register-role-icon">{role.icon}</div>
                  <div className="register-role-title">{role.title}</div>
                  <div className="register-role-desc">{role.description}</div>
                </button>
              ))}
            </div>
          </section>

          {/* Panel 3: datos personales */}
          {userType && (
            <section className="register-panel">
              <div className="register-panel-bar" />
              <h2 className="register-panel-title">3. Tus datos personales</h2>
              <div className="register-fields-group">
                <div className="register-grid-2">
                  <TextField
                    id="nombre"
                    name="nombre"
                    label="Nombre"
                    icon={User}
                    required
                    placeholder="Carlos"
                    value={baseData.nombre}
                    onChange={handleBaseChange}
                  />
                  <TextField
                    id="apellido"
                    name="apellido"
                    label="Apellido"
                    required
                    placeholder="Ruiz"
                    value={baseData.apellido}
                    onChange={handleBaseChange}
                  />
                </div>
                <div className="register-grid-2">
                  <TextField
                    id="dni"
                    name="dni"
                    label="DNI"
                    required
                    placeholder="XX.XXX.XXX"
                    value={baseData.dni}
                    onChange={handleBaseChange}
                  />
                  <TextField
                    id="telefono"
                    name="telefono"
                    type="tel"
                    label="Teléfono"
                    icon={Phone}
                    required
                    placeholder="+54 11 XXXX-XXXX"
                    value={baseData.telefono}
                    onChange={handleBaseChange}
                  />
                </div>
              </div>
            </section>
          )}

          {/* Panel 4: datos específicos del rol */}
          {userType && (
            <section className="register-panel">
              <div className="register-panel-bar" />
              <h2 className="register-panel-title">4. Datos de {activeRole?.title}</h2>

              {userType === "COMERCIO" && (
                <div className="register-fields-group">
                  <div className="register-grid-2">
                    <TextField
                      id="nombreComercial"
                      name="nombreComercial"
                      label="Nombre comercial"
                      required
                      placeholder="Ej: Urban Shoes"
                      value={extraData.nombreComercial ?? ""}
                      onChange={handleExtraChange}
                    />
                    <TextField
                      id="razonSocial"
                      name="razonSocial"
                      label="Razón social"
                      required
                      placeholder="Ej: Urban Shoes S.A."
                      value={extraData.razonSocial ?? ""}
                      onChange={handleExtraChange}
                    />
                  </div>
                  <TextField
                    id="cuit"
                    name="cuit"
                    label="CUIT"
                    required
                    placeholder="30-XXXXXXXX-X"
                    value={extraData.cuit ?? ""}
                    onChange={handleExtraChange}
                  />
                  <TextField
                    id="direccion"
                    name="direccion"
                    label="Dirección de retiro"
                    icon={MapPin}
                    required
                    placeholder="Av. Principal 123, Ciudad"
                    value={extraData.direccion ?? ""}
                    onChange={handleExtraChange}
                  />
                </div>
              )}

              {userType === "REPARTIDOR" && (
                <div className="register-fields-group">
                  <div className="register-field">
                    <label htmlFor="vehiculo" className="register-label">
                      Tipo de vehículo
                    </label>
                    <select
                      id="vehiculo"
                      name="vehiculo"
                      required
                      className="register-select"
                      value={extraData.vehiculo ?? ""}
                      onChange={handleExtraChange}
                    >
                      <option value="" disabled>
                        Seleccione...
                      </option>
                      <option value="MOTO">Moto</option>
                      <option value="AUTO">Auto / Utilitario</option>
                      <option value="BICI">Bicicleta</option>
                    </select>
                  </div>
                  <div className="register-grid-2">
                    <TextField
                      id="patente"
                      name="patente"
                      label="Patente"
                      icon={Hash}
                      required
                      maxLength={7}
                      placeholder="AB123CD"
                      title="Formato AAA999 o AA999AA"
                      value={extraData.patente ?? ""}
                      onChange={handleExtraChange}
                    />
                    <TextField
                      id="zona"
                      name="zona"
                      label="Zona de reparto"
                      icon={MapPin}
                      required
                      placeholder="Ej: Zona Sur"
                      value={extraData.zona ?? ""}
                      onChange={handleExtraChange}
                    />
                  </div>
                </div>
              )}
            </section>
          )}

          <Button
            type="submit"
            loading={loading}
            loadingText="Creando cuenta..."
            disabled={!userType || loading}
          >
            {`Crear cuenta${activeRole ? ` como ${activeRole.title}` : ""}`}
          </Button>
        </form>

        <p className="register-footer">
          ¿Ya tenés una cuenta? <Link to="/">Iniciá sesión aquí</Link>
        </p>
      </main>
    </div>
  );
}