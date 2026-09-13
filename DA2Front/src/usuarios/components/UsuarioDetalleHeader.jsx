import { EstadoBadge, RolBadge } from "./UsuarioBadge";

export function UsuarioDetalleHeader({ usuario }) {
  return (
    <div className="detalle-card-header">
      <span className="usuario-avatar usuario-avatar-lg">{usuario.nombre.charAt(0)}</span>
      <div className="detalle-nombre-bloque">
        <h2>{usuario.nombre} {usuario.apellido}</h2>
        <span className="usuario-email-chico">{usuario.email}</span>
      </div>
      <div className="detalle-badges">
        <RolBadge rol={usuario.rol} />
        <EstadoBadge estado={usuario.estado} />
      </div>
    </div>
  );
}