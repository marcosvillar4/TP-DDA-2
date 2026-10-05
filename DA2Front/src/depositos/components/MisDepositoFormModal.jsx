import { useState } from "react";
import "../styles/MisDepositos.css";

const initialForm = {
  nombreDeposito: "",
  direccionDeposito: "",
  nombre: "",
  apellido: "",
  dni: "",
  telefono: "",
  email: "",
  password: "",
  confirmPassword: "",
};

/**
 * Alta de un depósito por parte de un comercio.
 * Reutiliza los datos del antiguo registro de depósito: los del depósito
 * (nombre y dirección) y los de la cuenta del responsable que lo opera.
 */
export function MisDepositoFormModal({ submitting, onClose, onSubmit }) {
  const [form, setForm] = useState(initialForm);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const ok = await onSubmit(form);
    if (ok) onClose();
  }

  return (
    <div className="misdep-modal-backdrop" role="presentation">
      <div className="misdep-modal" role="dialog" aria-modal="true">
        <div className="misdep-modal-header">
          <div>
            <h2>Agregar depósito</h2>
            <p>Se vincula automáticamente a tu comercio.</p>
          </div>
          <button
            type="button"
            className="misdep-modal-close"
            onClick={onClose}
            aria-label="Cerrar"
          >
            ×
          </button>
        </div>

        <form className="misdep-form" onSubmit={handleSubmit}>
          <h3 className="misdep-form-section">Datos del depósito</h3>

          <label>
            <span>Nombre del depósito *</span>
            <input
              name="nombreDeposito"
              value={form.nombreDeposito}
              onChange={handleChange}
              placeholder="Depósito Palermo Central"
              required
            />
          </label>

          <label>
            <span>Dirección del depósito *</span>
            <input
              name="direccionDeposito"
              value={form.direccionDeposito}
              onChange={handleChange}
              placeholder="Av. Siempre Viva 742, CABA"
              required
            />
          </label>

          <h3 className="misdep-form-section">Responsable del depósito</h3>

          <div className="misdep-form-grid">
            <label>
              <span>Nombre *</span>
              <input name="nombre" value={form.nombre} onChange={handleChange} required />
            </label>
            <label>
              <span>Apellido *</span>
              <input name="apellido" value={form.apellido} onChange={handleChange} required />
            </label>
            <label>
              <span>DNI *</span>
              <input name="dni" value={form.dni} onChange={handleChange} required />
            </label>
            <label>
              <span>Teléfono *</span>
              <input
                type="tel"
                name="telefono"
                value={form.telefono}
                onChange={handleChange}
                placeholder="+54 11 XXXX-XXXX"
                required
              />
            </label>
          </div>

          <label>
            <span>Correo electrónico *</span>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="deposito@correo.com"
              required
            />
          </label>

          <div className="misdep-form-grid">
            <label>
              <span>Contraseña *</span>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                minLength={8}
                required
              />
            </label>
            <label>
              <span>Confirmar contraseña *</span>
              <input
                type="password"
                name="confirmPassword"
                value={form.confirmPassword}
                onChange={handleChange}
                minLength={8}
                required
              />
            </label>
          </div>

          <p className="misdep-form-note">
            La cuenta del responsable queda pendiente de validación por un administrador.
          </p>

          <div className="misdep-form-actions">
            <button type="button" className="misdep-btn-cancel" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="misdep-btn-submit" disabled={submitting}>
              {submitting ? "Guardando..." : "Agregar depósito"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
