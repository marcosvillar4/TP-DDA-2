import { EstadoBadge, RolBadge } from "./UsuarioBadge";

export function UsuarioResumenBox({ usuario }) {
  return (
    <div className="resumen-box">
      <h4>Resumen</h4>
      <div className="resumen-fila"><span>Rol</span><RolBadge rol={usuario.rol} /></div>
      <div className="resumen-fila"><span>Estado</span><EstadoBadge estado={usuario.estado} /></div>
    </div>
  );
}