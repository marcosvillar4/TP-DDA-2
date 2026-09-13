import { useUsuarios } from "../../hooks/useUsuarios";
import { UsuariosStats } from "./components/UsuariosStats";
import { UsuariosFiltros } from "./components/UsuariosFiltros";
import { UsuariosTabla } from "./components/UsuariosTabla";
import "./styles/Usuarios.css";

export default function UsuariosPage() {
  const {
    usuarios, stats, loading, error,
    busqueda, setBusqueda,
    filtroRol, setFiltroRol,
    filtroEstado, setFiltroEstado,
  } = useUsuarios();

  if (loading) return <div className="usuarios-page">Cargando usuarios...</div>;
  if (error) return <div className="usuarios-page usuarios-error">{error}</div>;

  return (
    <div className="usuarios-page">
      <div className="usuarios-header">
        <div>
          <h1>Usuarios</h1>
          <p className="usuarios-subtitulo">Administración de usuarios de LogiRed</p>
        </div>
      </div>

      <UsuariosStats stats={stats} />
      <UsuariosFiltros
        busqueda={busqueda} setBusqueda={setBusqueda}
        filtroRol={filtroRol} setFiltroRol={setFiltroRol}
        filtroEstado={filtroEstado} setFiltroEstado={setFiltroEstado}
      />
      <UsuariosTabla usuarios={usuarios} />
    </div>
  );
}