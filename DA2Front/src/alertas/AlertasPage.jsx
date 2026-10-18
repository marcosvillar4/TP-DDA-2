import { useNavigate } from "react-router-dom";
import {
  AlertCircle,
  Bell,
  Check,
  Info,
  RefreshCw,
  TriangleAlert,
} from "lucide-react";
import { useNotificaciones } from "../../hooks/useNotificaciones";
import "./AlertasPage.css";

const TIPO_CONFIG = {
  STOCK_BAJO: { label: "Stock bajo", className: "alerta-stock", icon: TriangleAlert },
  ACTUALIZACION_PEDIDO: {
    label: "Pedido actualizado",
    className: "alerta-pedido",
    icon: Info,
  },
  SOLICITUD_REGISTRO: {
    label: "Solicitud de registro",
    className: "alerta-registro",
    icon: Bell,
  },
};

function formatoFecha(fecha) {
  if (!fecha) return "";
  const valor = new Date(fecha);
  if (Number.isNaN(valor.getTime())) return "";
  return valor.toLocaleString("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function configuracionDe(tipo) {
  return TIPO_CONFIG[tipo] ?? {
    label: "Notificación",
    className: "alerta-generica",
    icon: AlertCircle,
  };
}

export default function AlertasPage() {
  const navigate = useNavigate();
  const { notificaciones, loading, error, recargar, marcarLeida } =
    useNotificaciones();
  const noLeidas = notificaciones.filter((item) => !item.leida).length;

  function abrirNotificacion(notificacion) {
    marcarLeida(notificacion);
    if (
      notificacion.referenciaId &&
      notificacion.tipo === "ACTUALIZACION_PEDIDO"
    ) {
      navigate(`/pedidos/${notificacion.referenciaId}`);
    }
  }

  return (
    <div className="alertas-page">
      <div className="alertas-page-header">
        <div>
          <h1 className="alertas-page-title">Alertas</h1>
          <p className="alertas-page-subtitle">
            {noLeidas === 0
              ? "No tenés notificaciones pendientes."
              : `${noLeidas} notificación${noLeidas === 1 ? "" : "es"} sin leer`}
          </p>
        </div>
        <button
          type="button"
          className="alertas-refresh"
          onClick={recargar}
          disabled={loading}
          title="Actualizar notificaciones"
        >
          <RefreshCw size={16} className={loading ? "alertas-spin" : ""} />
          Actualizar
        </button>
      </div>

      {error && (
        <div className="alertas-error" role="alert">
          <span>{error}</span>
          <button type="button" onClick={recargar}>
            Reintentar
          </button>
        </div>
      )}

      <section className="alertas-page-card">
        {loading ? (
          <p className="alertas-state">Cargando notificaciones...</p>
        ) : notificaciones.length === 0 ? (
          <div className="alertas-empty">
            <Bell size={28} />
            <strong>No hay notificaciones</strong>
            <span>Cuando haya novedades, aparecerán acá.</span>
          </div>
        ) : (
          <ul className="notificaciones-list">
            {notificaciones.map((notificacion) => {
              const config = configuracionDe(notificacion.tipo);
              const Icon = config.icon;
              return (
                <li
                  key={notificacion.id}
                  className={`notificacion-item ${config.className} ${
                    notificacion.leida ? "notificacion-leida" : ""
                  }`}
                >
                  <button
                    type="button"
                    className="notificacion-content"
                    onClick={() => abrirNotificacion(notificacion)}
                  >
                    <span className="notificacion-icon">
                      <Icon size={18} />
                    </span>
                    <span className="notificacion-text">
                      <span className="notificacion-heading">
                        <strong>{notificacion.titulo}</strong>
                        {!notificacion.leida && (
                          <span className="notificacion-nueva">Nueva</span>
                        )}
                      </span>
                      <span className="notificacion-message">
                        {notificacion.mensaje}
                      </span>
                      <span className="notificacion-date">
                        {config.label} · {formatoFecha(notificacion.fechaCreacion)}
                      </span>
                    </span>
                  </button>
                  {!notificacion.leida && (
                    <button
                      type="button"
                      className="notificacion-check"
                      onClick={() => marcarLeida(notificacion)}
                      title="Marcar como leída"
                    >
                      <Check size={16} />
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </section>
    </div>
  );
}
