import { useNavigate, useParams } from "react-router-dom";
import { useUsuarioDetail } from "../../hooks/useUsuarioDetail";
import { useEditarUsuarioAdmin } from "../../hooks/useEditarUsuarioAdmin";
import { UsuarioDetalleHeader } from "./components/UsuarioDetalleHeader";
import { UsuarioDatosCard } from "./components/UsuarioDatosCard";
import { UsuarioEditarForm } from "./components/UsuarioEditarForm";
import { UsuarioAccionesEstado } from "./components/UsuarioAccionesEstado";
import { UsuarioResumenBox } from "./components/UsuarioResumenBox";
import "./styles/Usuarios.css";

export default function UsuarioDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const {
    usuario, setUsuario, loading, error, accionEnCurso,
    validar, rechazar, bloquear, desbloquear,
  } = useUsuarioDetail(id);

  const edicion = useEditarUsuarioAdmin(usuario, setUsuario);

  if (loading) return <div className="usuarios-page">Cargando...</div>;
  if (error) return <div className="usuarios-page usuarios-error">{error}</div>;
  if (!usuario) return null;

  return (
    <div className="usuarios-page">
      <button className="volver-link" onClick={() => navigate("/usuarios")}>← Volver a Usuarios</button>

      <div className="detalle-grid">
        <div className="detalle-card">
          <UsuarioDetalleHeader usuario={usuario} />

          {edicion.editando ? (
            <UsuarioEditarForm
              form={edicion.form}
              guardando={edicion.guardando}
              handleChange={edicion.handleChange}
              guardar={edicion.guardar}
              cancelar={edicion.cancelar}
            />
          ) : (
            <UsuarioDatosCard usuario={usuario} />
          )}
        </div>

        <div className="acciones-card">
          <h3>Acciones</h3>

          {!edicion.editando && (
            <>
              <button className="btn-accion btn-editar" onClick={edicion.iniciarEdicion}>✎ Editar usuario</button>
              <button className="btn-accion btn-reset" onClick={edicion.resetearPassword}>🔑 Resetear contraseña</button>
            </>
          )}

          <UsuarioAccionesEstado
            usuario={usuario}
            accionEnCurso={accionEnCurso}
            validar={validar}
            rechazar={rechazar}
            bloquear={bloquear}
            desbloquear={desbloquear}
          />

          <UsuarioResumenBox usuario={usuario} />
        </div>
      </div>
    </div>
  );
}