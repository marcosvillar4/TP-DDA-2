import { EstadoBadge, RolBadge } from "./UsuarioBadge";

export function UsuarioResumenBox({ usuario }) {
  return (
    <div className="resumen-box">
      <div className="resumen-fila"><span>Rol</span><RolBadge rol={usuario.rol} /></div>
      <div className="resumen-fila"><span>Estado</span><EstadoBadge estado={usuario.estado} /></div>
      <div className="resumen-fila"><span>ID de usuario</span><strong>#{usuario.id}</strong></div>
    </div>
  );
}