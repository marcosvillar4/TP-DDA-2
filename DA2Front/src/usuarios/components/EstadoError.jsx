import { AlertTriangle, RefreshCw } from "lucide-react";

export function EstadoError({ mensaje, onReintentar }) {
  return (
    <div className="usuarios-error" role="alert">
      <span className="usuarios-error-icono"><AlertTriangle size={22} /></span>
      <h4>No se pudo cargar la información</h4>
      <p>{mensaje}</p>
      {onReintentar && (
        <button type="button" className="u-btn u-btn-ghost" onClick={onReintentar}>
          <RefreshCw size={15} /> Reintentar
        </button>
      )}
    </div>
  );
}