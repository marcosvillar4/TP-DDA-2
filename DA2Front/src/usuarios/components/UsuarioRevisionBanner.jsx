import { Clock, Check, X } from "lucide-react";
import { ROLE_LABELS } from "../../layout/navConfig";

/** Barra de decisión para usuarios EN_EVALUACION: queda junto a los datos que se están revisando. */
export function UsuarioRevisionBanner({ usuario, accionEnCurso, bloqueado, onValidar, onRechazar, posicion, total }) {
  const rol = (ROLE_LABELS[usuario.rol] ?? usuario.rol).toLowerCase();
  const deshabilitado = accionEnCurso || bloqueado;

  return (
    <div className="revision-banner" role="status">
      <span className="revision-icono"><Clock size={20} /></span>
      <div className="revision-texto">
        <h3>
          Pendiente de evaluación
          {total > 1 && <span className="revision-cola"> · {posicion} de {total}</span>}
        </h3>
        <p>Revisá los datos del {rol} y decidí si habilitás su acceso a la plataforma.</p>
      </div>
      <div className="revision-botones">
        <button type="button" className="u-btn u-btn-danger-ghost" disabled={deshabilitado} onClick={onRechazar}>
          <X size={16} /> Rechazar
        </button>
        <button type="button" className="u-btn u-btn-success" disabled={deshabilitado} onClick={onValidar}>
          <Check size={16} /> Validar usuario
        </button>
      </div>
    </div>
  );
}