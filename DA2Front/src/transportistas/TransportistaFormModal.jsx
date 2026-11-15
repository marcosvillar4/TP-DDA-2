import { useState } from "react";
import { X } from "lucide-react";
import "./styles/TransportistaFormModal.css";

export default function TransportistaFormModal({ open, transportista, saving, onClose, onSubmit }) {
  const [empresa, setEmpresa] = useState(transportista?.empresa || "");
  if (!open) return null;

  function submit(event) {
    event.preventDefault();
    onSubmit({ empresa: empresa.trim() });
  }

  return (
    <div className="transportista-modal-backdrop" role="presentation">
      <div className="transportista-modal" role="dialog" aria-modal="true">
        <header className="transportista-modal-header">
          <div><h2>{transportista ? "Editar transportista" : "Nuevo transportista"}</h2><p>Configurá la empresa de transporte externo.</p></div>
          <button type="button" className="transportista-close" onClick={onClose} aria-label="Cerrar"><X size={18} /></button>
        </header>
        <form className="transportista-form" onSubmit={submit}>
          <label><span>Empresa *</span><input value={empresa} onChange={(event) => setEmpresa(event.target.value)} required maxLength={120} autoFocus /></label>
          <div className="transportista-form-actions">
            <button type="button" className="transportista-secondary" onClick={onClose}>Cancelar</button>
            <button type="submit" className="transportista-primary" disabled={saving}>{saving ? "Guardando..." : "Guardar"}</button>
          </div>
        </form>
      </div>
    </div>
  );
}
