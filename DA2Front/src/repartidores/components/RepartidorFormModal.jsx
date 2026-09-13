import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";
import "../styles/RepartidorFormModal.css";

const initialForm = {
  usuarioId: "",
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
        patente: repartidor.patente || "",
        zona: repartidor.zona || "",
      });
    } else {
      setForm(initialForm);
    }
  }, [open, mode, repartidor]);

  const usuarioSeleccionado = useMemo(
    () => usuariosDisponibles.find((usuario) => String(usuario.id) === String(form.usuarioId)),
    [usuariosDisponibles, form.usuarioId]
  );

  if (!open) return null;

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();

    const payload = {
      patente: form.patente.trim(),
      zona: form.zona.trim(),
    };

    if (mode === "create") {
      payload.usuarioId = Number(form.usuarioId);
    }

    onSubmit(payload);
  }

  const title = mode === "edit" ? "Editar repartidor" : "Nuevo repartidor";
  const usuarioActual = mode === "edit" ? repartidor : usuarioSeleccionado;

  return (
    <div className="repartidor-modal-backdrop" role="presentation">
      <div className="repartidor-modal" role="dialog" aria-modal="true">
        <div className="repartidor-modal-header">
          <div>
            <h2>{title}</h2>
            <p>
              {mode === "edit"
                ? "Actualizá los datos operativos del perfil."
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
                    {usuario.nombreCompleto} - {usuario.email}
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
              <strong>{repartidor?.nombreCompleto}</strong>
              <small>{repartidor?.email}</small>
            </div>
          )}

          {usuarioActual && (
            <div className="repartidor-readonly-grid">
              <div>
                <span>Teléfono</span>
                <strong>{usuarioActual.telefono || "—"}</strong>
              </div>
              <div>
                <span>Vehículo</span>
                <strong>{usuarioActual.vehiculo || "—"}</strong>
              </div>
            </div>
          )}

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
