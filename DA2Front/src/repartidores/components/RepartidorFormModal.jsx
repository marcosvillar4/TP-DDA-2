import { useEffect, useState } from "react";
import { X } from "lucide-react";
import "../styles/RepartidorFormModal.css";

const initialForm = {
  usuarioId: "",
  nombre: "",
  apellido: "",
  telefono: "",
  tipoVehiculo: "",
  patente: "",
  zona: "",
};

export default function RepartidorFormModal({
  open,
  mode = "create",
  repartidor = null,
  usuariosDisponibles = [],
  saving = false,
  onClose,
  onSubmit,
}) {
  const [form, setForm] = useState(initialForm);

  useEffect(() => {
    if (!open) return;

    if (mode === "edit" && repartidor) {
      setForm({
        usuarioId: repartidor.usuarioId || "",
        nombre: repartidor.nombre || "",
        apellido: repartidor.apellido || "",
        telefono: repartidor.telefono || "",
        tipoVehiculo: repartidor.tipoVehiculo || "",
        patente: repartidor.patente || "",
        zona: repartidor.zona || "",
      });
    } else {
      setForm(initialForm);
    }
  }, [open, mode, repartidor]);

  if (!open) return null;

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();

    const payload = {
      nombre: form.nombre.trim(),
      apellido: form.apellido.trim(),
      telefono: form.telefono.trim(),
      tipoVehiculo: form.tipoVehiculo.trim(),
      patente: form.patente.trim(),
      zona: form.zona.trim(),
    };

    if (mode === "create") {
      payload.usuarioId = Number(form.usuarioId);
    }

    onSubmit(payload);
  }

  const title = mode === "edit" ? "Editar repartidor" : "Nuevo repartidor";

  return (
    <div className="repartidor-modal-backdrop" role="presentation">
      <div className="repartidor-modal" role="dialog" aria-modal="true">
        <div className="repartidor-modal-header">
          <div>
            <h2>{title}</h2>
            <p>
              {mode === "edit"
                ? "Actualizá los datos operativos del repartidor."
                : "Completá el perfil operativo de un usuario con rol Repartidor."}
            </p>
          </div>
          <button className="repartidor-modal-close" onClick={onClose} type="button">
            <X size={18} />
          </button>
        </div>

        <form className="repartidor-form" onSubmit={handleSubmit}>
          {mode === "create" ? (
            <label className="repartidor-field repartidor-field-full">
              <span>Usuario repartidor *</span>
              <select
                name="usuarioId"
                value={form.usuarioId}
                onChange={handleChange}
                required
                disabled={saving || usuariosDisponibles.length === 0}
              >
                <option value="">Seleccionar usuario...</option>
                {usuariosDisponibles.map((usuario) => (
                  <option key={usuario.id} value={usuario.id}>
                    {usuario.username} - {usuario.email}
                  </option>
                ))}
              </select>
              {usuariosDisponibles.length === 0 && (
                <small>No hay usuarios REPARTIDOR disponibles. Primero registrá una cuenta de repartidor.</small>
              )}
            </label>
          ) : (
            <div className="repartidor-readonly-block">
              <span>Usuario asociado</span>
              <strong>{repartidor?.email}</strong>
            </div>
          )}

          <label className="repartidor-field">
            <span>Nombre *</span>
            <input name="nombre" value={form.nombre} onChange={handleChange} required />
          </label>

          <label className="repartidor-field">
            <span>Apellido *</span>
            <input name="apellido" value={form.apellido} onChange={handleChange} required />
          </label>

          <label className="repartidor-field">
            <span>Teléfono *</span>
            <input name="telefono" value={form.telefono} onChange={handleChange} required />
          </label>

          <label className="repartidor-field">
            <span>Tipo de vehículo *</span>
            <input
              name="tipoVehiculo"
              value={form.tipoVehiculo}
              onChange={handleChange}
              placeholder="Moto, camioneta, furgón..."
              required
            />
          </label>

          <label className="repartidor-field">
            <span>Patente *</span>
            <input name="patente" value={form.patente} onChange={handleChange} required />
          </label>

          <label className="repartidor-field">
            <span>Zona *</span>
            <input name="zona" value={form.zona} onChange={handleChange} required />
          </label>

          <div className="repartidor-form-actions">
            <button type="button" className="repartidor-btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="repartidor-btn-primary" disabled={saving}>
              {saving ? "Guardando..." : "Guardar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
