import { useMemo } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Pencil, KeyRound, ShieldCheck, User } from "lucide-react";
import { useUsuarioDetail } from "../../hooks/useUsuarioDetail";
import { useEditarUsuarioAdmin } from "../../hooks/useEditarUsuarioAdmin";
import { useEditarPerfilAdmin } from "../../hooks/useEditarPerfilAdmin";
import { Panel } from "./components/Panel";
import { EstadoError } from "./components/EstadoError";
import { UsuarioDetalleHeader } from "./components/UsuarioDetalleHeader";
import { UsuarioRevisionBanner } from "./components/UsuarioRevisionBanner";
import { UsuarioDatosCard } from "./components/UsuarioDatosCard";
import { UsuarioEditarForm } from "./components/UsuarioEditarForm";
import { UsuarioPerfilSection } from "./components/UsuarioPerfilSection";
import { UsuarioAccionesEstado } from "./components/UsuarioAccionesEstado";
import { UsuarioResumenBox } from "./components/UsuarioResumenBox";
import "./styles/Usuarios.css";

function DetalleSkeleton() {
  return (
    <>
      <div className="skeleton detalle-skeleton-hero" />
      <div className="detalle-grid">
        <div className="detalle-col">
          <div className="skeleton detalle-skeleton-panel" />
          <div className="skeleton detalle-skeleton-panel" />
        </div>
        <div className="detalle-col">
          <div className="skeleton detalle-skeleton-panel" />
        </div>
      </div>
    </>
  );
}

export default function UsuarioDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const {
    usuario, setUsuario, loading, error, accionEnCurso, recargar,
    validar, rechazar, bloquear, desbloquear,
  } = useUsuarioDetail(id);

  // Las respuestas se mezclan con lo que ya hay: el PATCH de datos personales devuelve
  // el usuario sin "perfil", y el del perfil devuelve el detalle completo ya actualizado.
  const actualizarLocal = (u) => setUsuario((prev) => ({ ...prev, ...u }));
  const edicion = useEditarUsuarioAdmin(usuario, actualizarLocal);
  const edicionPerfil = useEditarPerfilAdmin(usuario, actualizarLocal);

  // Cola de revisión: la manda la lista cuando se entra desde un usuario pendiente.
  const cola = useMemo(() => (location.state?.cola ?? []).map(String), [location.state]);
  const posicion = cola.indexOf(String(id));

  const volver = () =>
    location.key !== "default" ? navigate(-1) : navigate("/usuarios");

  async function resolver(accion) {
    const ok = await accion();
    if (!ok || posicion === -1) return;

    edicion.cancelar();
    edicionPerfil.cancelar();
    const siguiente = cola[posicion + 1];
    if (siguiente) {
      navigate(`/usuarios/${siguiente}`, { replace: true, state: { cola } });
    } else {
      navigate("/usuarios?estado=EN_EVALUACION", { replace: true });
    }
  }

  return (
    <div className="usuarios-page">
      <div className="detalle-topbar">
        <button type="button" className="volver-link" onClick={volver}>
          <ArrowLeft size={16} /> Volver
        </button>
      </div>

      {loading ? (
        <DetalleSkeleton />
      ) : error ? (
        <EstadoError mensaje={error} onReintentar={recargar} />
      ) : !usuario ? (
        <EstadoError mensaje="No se encontró el usuario." />
      ) : (
        <>
          <UsuarioDetalleHeader usuario={usuario} />

          {usuario.estado === "EN_EVALUACION" && (
            <UsuarioRevisionBanner
              usuario={usuario}
              accionEnCurso={accionEnCurso}
              bloqueado={edicion.editando || edicionPerfil.editando}
              onValidar={() => resolver(validar)}
              onRechazar={() => resolver(rechazar)}
              posicion={posicion + 1}
              total={cola.length}
            />
          )}

          <div className="detalle-grid">
            <div className="detalle-col">
              <Panel
                titulo="Información personal"
                icono={User}
                acciones={
                  !edicion.editando && (
                    <button type="button" className="u-btn u-btn-ghost" onClick={edicion.iniciarEdicion}>
                      <Pencil size={14} /> Editar
                    </button>
                  )
                }
              >
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
              </Panel>

              <UsuarioPerfilSection usuario={usuario} edicion={edicionPerfil} />
            </div>

            <div className="detalle-col">
              <Panel titulo="Estado de la cuenta" icono={ShieldCheck}>
                <UsuarioResumenBox usuario={usuario} />
                <UsuarioAccionesEstado
                  usuario={usuario}
                  accionEnCurso={accionEnCurso}
                  bloquear={bloquear}
                  desbloquear={desbloquear}
                />
              </Panel>

              <Panel titulo="Seguridad" icono={KeyRound}>
                <button type="button" className="btn-accion btn-reset" onClick={edicion.resetearPassword}>
                  <KeyRound size={17} /> Resetear contraseña
                </button>
              </Panel>
            </div>
          </div>
        </>
      )}
    </div>
  );
}