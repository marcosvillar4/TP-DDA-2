import { Ban, ShieldCheck } from "lucide-react";

/** Acciones de cuenta. Validar/rechazar viven en el banner de revisión, junto a los datos. */
export function UsuarioAccionesEstado({ usuario, accionEnCurso, bloquear, desbloquear }) {
  switch (usuario.estado) {
    case "VALIDADO":
      return (
        <button type="button" className="btn-accion btn-bloquear" disabled={accionEnCurso} onClick={bloquear}>
          <Ban size={17} /> Bloquear usuario
        </button>
      );
    case "BLOQUEADO":
      return (
        <button type="button" className="btn-accion btn-desbloquear" disabled={accionEnCurso} onClick={desbloquear}>
          <ShieldCheck size={17} /> Desbloquear usuario
        </button>
      );
    case "EN_EVALUACION":
      return <p className="acciones-vacio">Validá o rechazá al usuario desde la barra de revisión.</p>;
    default:
      return <p className="acciones-vacio">Este usuario fue rechazado. No hay acciones disponibles.</p>;
  }
}