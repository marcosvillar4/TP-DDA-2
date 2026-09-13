export function UsuarioAccionesEstado({ usuario, accionEnCurso, validar, rechazar, bloquear, desbloquear }) {
  if (usuario.estado === "EN_EVALUACION") {
    return (
      <>
        <button className="btn-accion btn-validar" disabled={accionEnCurso} onClick={validar}>✓ Validar usuario</button>
        <button className="btn-accion btn-rechazar" disabled={accionEnCurso} onClick={rechazar}>✕ Rechazar usuario</button>
      </>
    );
  }
  if (usuario.estado === "VALIDADO") {
    return <button className="btn-accion btn-bloquear" disabled={accionEnCurso} onClick={bloquear}>✕ Bloquear usuario</button>;
  }
  if (usuario.estado === "BLOQUEADO") {
    return <button className="btn-accion btn-desbloquear" disabled={accionEnCurso} onClick={desbloquear}>✓ Desbloquear usuario</button>;
  }
  return <p className="acciones-vacio">Este usuario fue rechazado. No hay acciones disponibles.</p>;
}