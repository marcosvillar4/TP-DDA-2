import { useNavigate } from "react-router-dom";
import { EstadoBadge, RolBadge } from "./UsuarioBadge";

export function UsuariosTabla({ usuarios }) {
  const navigate = useNavigate();

  return (
    <table className="usuarios-tabla">
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Email</th>
          <th>Rol</th>
          <th>Estado</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {usuarios.map((u) => (
          <tr key={u.id}>
            <td>
              <div className="usuario-nombre-cell">
                <span className="usuario-avatar">{u.nombre.charAt(0)}</span>
                {u.nombre} {u.apellido}
              </div>
            </td>
            <td>{u.email}</td>
            <td><RolBadge rol={u.rol} /></td>
            <td><EstadoBadge estado={u.estado} /></td>
            <td>
              <button className="btn-ver" onClick={() => navigate(`/usuarios/${u.id}`)}>Ver</button>
            </td>
          </tr>
        ))}
        {usuarios.length === 0 && (
          <tr>
            <td colSpan={5} className="usuarios-vacio">No se encontraron usuarios.</td>
          </tr>
        )}
      </tbody>
    </table>
  );
}