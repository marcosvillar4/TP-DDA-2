import { RefreshCw } from "lucide-react";
import { useUsuarios } from "../../hooks/useUsuarios";
import { UsuariosStats } from "./components/UsuariosStats";
import { UsuariosFiltros } from "./components/UsuariosFiltros";
import { UsuariosTabla } from "./components/UsuariosTabla";
import { UsuariosPaginacion } from "./components/UsuariosPaginacion";
import { EstadoError } from "./components/EstadoError";
import "./styles/Usuarios.css";

export default function UsuariosPage() {
  const {
    usuarios, totalFiltrados, stats, loading, error, recargar, colaPendientes,
    busqueda, setBusqueda,
    filtroRol, setFiltroRol,
    filtroEstado, setFiltroEstado,
    hayFiltros, limpiarFiltros,
    pagina, totalPaginas, porPagina, setPagina,
  } = useUsuarios();

  return (
    <div className="usuarios-page">
      <header className="usuarios-header">
        <div>
          <h1>Usuarios</h1>
          <p className="usuarios-subtitulo">Administración de usuarios de LogiRed</p>
        </div>
        <button type="button" className="u-btn u-btn-ghost" onClick={recargar} disabled={loading}>
          <RefreshCw size={15} className={loading ? "u-girando" : ""} /> Actualizar
        </button>
      </header>

      <UsuariosStats
        stats={stats}
        loading={loading}
        estadoActivo={filtroEstado}
        onSelect={(clave) => setFiltroEstado(clave === filtroEstado ? "TODOS" : clave)}
      />

      <UsuariosFiltros
        busqueda={busqueda} setBusqueda={setBusqueda}
        filtroRol={filtroRol} setFiltroRol={setFiltroRol}
        hayFiltros={hayFiltros} onLimpiar={limpiarFiltros}
      />

      {error ? (
        <EstadoError mensaje={error} onReintentar={recargar} />
      ) : (
        <div className="usuarios-tabla-card">
          <UsuariosTabla
            usuarios={usuarios}
            loading={loading}
            cola={colaPendientes}
            hayFiltros={hayFiltros}
            onLimpiar={limpiarFiltros}
          />
          {!loading && (
            <UsuariosPaginacion
              total={totalFiltrados}
              pagina={pagina}
              totalPaginas={totalPaginas}
              porPagina={porPagina}
              onCambiar={setPagina}
            />
          )}
        </div>
      )}
    </div>
  );
}