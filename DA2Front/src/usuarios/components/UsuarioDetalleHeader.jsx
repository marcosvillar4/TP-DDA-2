import { Mail, Phone } from "lucide-react";
import { EstadoBadge, RolBadge } from "./UsuarioBadge";
import { iniciales } from "../utils/usuarioLabels";

export function UsuarioDetalleHeader({ usuario }) {
  return (
    <div className="detalle-hero">
      <span className="usuario-avatar usuario-avatar-lg">{iniciales(usuario)}</span>
      <div className="detalle-hero-info">
        <h1>{usuario.nombre} {usuario.apellido}</h1>
        <div className="detalle-hero-meta">
          <span><Mail size={14} /> {usuario.email}</span>
          {usuario.telefono && <span><Phone size={14} /> {usuario.telefono}</span>}
        </div>
      </div>
    </div>
  );
}