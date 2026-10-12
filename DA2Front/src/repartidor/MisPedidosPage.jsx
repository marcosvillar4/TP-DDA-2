import { useCallback, useEffect, useRef, useState } from "react";
import { Clock, MapPin, PackageCheck, RefreshCw, Route, Truck } from "lucide-react";
import { getMiHistorialRepartidor, getMiPerfilRepartidor } from "../../api/repartidorPerfilApi";
import BadgeEstado from "../pedidos/components/BadgeEstado";
import RepartidorEstadoBadge from "../repartidores/components/RepartidorEstadoBadge";
import "./styles/MisPedidosPage.css";

export default function MisPedidosPage() {
  const [perfil, setPerfil] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [loadingPerfil, setLoadingPerfil] = useState(true);
  const [loadingHistorial, setLoadingHistorial] = useState(true);
  const [errorPerfil, setErrorPerfil] = useState(null);
  const [errorHistorial, setErrorHistorial] = useState(null);
  const mountedRef = useRef(false);

  const cargarPerfil = useCallback(async () => {
    setLoadingPerfil(true);
    setErrorPerfil(null);

    try {
      const perfilData = await getMiPerfilRepartidor();
      if (!mountedRef.current) return;
      setPerfil(perfilData);
    } catch (err) {
      if (!mountedRef.current) return;
      setPerfil(null);
      setErrorPerfil(normalizarError(err));
    } finally {
      if (mountedRef.current) {
        setLoadingPerfil(false);
      }
    }
  }, []);

  const cargarHistorial = useCallback(async () => {
    setLoadingHistorial(true);
    setErrorHistorial(null);

    try {
      const historialData = await getMiHistorialRepartidor();
      if (!mountedRef.current) return;
      setHistorial(Array.isArray(historialData) ? historialData : []);
    } catch (err) {
      if (!mountedRef.current) return;
      setHistorial([]);
      setErrorHistorial(normalizarError(err));
    } finally {
      if (mountedRef.current) {
        setLoadingHistorial(false);
      }
    }
  }, []);

  useEffect(() => {
    mountedRef.current = true;

    async function cargarPerfilInicial() {
      try {
        const perfilData = await getMiPerfilRepartidor();
        if (!mountedRef.current) return;
        setPerfil(perfilData);
      } catch (err) {
        if (!mountedRef.current) return;
        setPerfil(null);
        setErrorPerfil(normalizarError(err));
      } finally {
        if (mountedRef.current) {
          setLoadingPerfil(false);
        }
      }
    }

    async function cargarHistorialInicial() {
      try {
        const historialData = await getMiHistorialRepartidor();
        if (!mountedRef.current) return;
        setHistorial(Array.isArray(historialData) ? historialData : []);
      } catch (err) {
        if (!mountedRef.current) return;
        setHistorial([]);
        setErrorHistorial(normalizarError(err));
      } finally {
        if (mountedRef.current) {
          setLoadingHistorial(false);
        }
      }
    }

    cargarPerfilInicial();
    cargarHistorialInicial();

    return () => {
      mountedRef.current = false;
    };
  }, []);

  if (loadingPerfil) {
    return <EstadoPagina titulo="Cargando tus pedidos..." texto="Estamos consultando tu perfil operativo." />;
  }

  if (errorPerfil) {
    return (
      <EstadoPagina
        titulo={errorPerfil.titulo}
        texto={errorPerfil.texto}
        accion={
          errorPerfil.reintentar ? (
            <button className="mis-pedidos-btn" onClick={cargarPerfil} type="button">
              <RefreshCw size={16} />
              Reintentar
            </button>
          ) : null
        }
        error
      />
    );
  }

  const pedidoActual = perfil?.pedidoActual ?? null;

  return (
    <div className="mis-pedidos-page">
      <header className="mis-pedidos-header">
        <div>
          <span className="mis-pedidos-eyebrow">Repartidor</span>
          <h1>Mis Pedidos</h1>
          <p>Consultá tu pedido asignado y el historial de entregas finalizadas.</p>
        </div>
        {perfil && <RepartidorEstadoBadge estado={perfil.estado} activo={perfil.activo} />}
      </header>

      <section className="mis-pedidos-grid">
        <article className="mis-pedidos-card mis-pedidos-current">
          <div className="mis-pedidos-card-header">
            <div>
              <h2>Pedido actual</h2>
              <p>Información disponible para tu operación en curso.</p>
            </div>
            <Truck size={22} />
          </div>

          {pedidoActual ? (
            <PedidoActual pedido={pedidoActual} />
          ) : (
            <div className="mis-pedidos-empty">
              <PackageCheck size={24} />
              <strong>No tenés pedidos asignados en este momento</strong>
              <span>Cuando el equipo operativo te asigne un pedido, vas a verlo acá.</span>
            </div>
          )}
        </article>

        <aside className="mis-pedidos-card mis-pedidos-profile">
          <h2>Tu perfil operativo</h2>
          <Info label="Nombre" value={perfil?.nombreCompleto || nombreCompleto(perfil)} />
          <Info label="Vehículo" value={formatVehiculo(perfil?.vehiculo)} />
          <Info label="Patente" value={perfil?.patente} />
          <Info label="Zona" value={perfil?.zona} />
        </aside>
      </section>

      <section className="mis-pedidos-card">
        <div className="mis-pedidos-card-header">
          <div>
            <h2>Historial de pedidos</h2>
            <p>Entregas finalizadas asociadas a tu perfil.</p>
          </div>
          <span className="mis-pedidos-count">{historial.length} registro{historial.length !== 1 ? "s" : ""}</span>
        </div>

        <div className="mis-pedidos-table-wrapper">
          {errorHistorial ? (
            <div className="mis-pedidos-section-error" role="alert">
              <strong>{errorHistorial.titulo}</strong>
              <span>{errorHistorial.texto}</span>
              {errorHistorial.reintentar && (
                <button className="mis-pedidos-btn" onClick={cargarHistorial} type="button">
                  <RefreshCw size={16} />
                  Reintentar historial
                </button>
              )}
            </div>
          ) : (
            <table className="mis-pedidos-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Dirección de entrega</th>
                  <th>Estado final</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                {loadingHistorial && (
                  <tr>
                    <td colSpan="4" className="mis-pedidos-table-empty">
                      Cargando historial...
                    </td>
                  </tr>
                )}
                {!loadingHistorial && historial.map((item) => (
                  <tr key={item.pedidoId}>
                    <td className="mis-pedidos-id">#{item.pedidoId}</td>
                    <td>{item.direccionEntrega || "—"}</td>
                    <td>{formatResultado(item.resultado)}</td>
                    <td>{item.fecha || "—"}</td>
                  </tr>
                ))}
                {!loadingHistorial && historial.length === 0 && (
                  <tr>
                    <td colSpan="4" className="mis-pedidos-table-empty">
                      Todavía no tenés entregas registradas
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </div>
  );
}

function PedidoActual({ pedido }) {
  return (
    <div className="mis-pedidos-current-body">
      <div className="mis-pedidos-current-main">
        <span className="mis-pedidos-id">#{pedido.id}</span>
        <BadgeEstado estado={pedido.estado} />
      </div>

      <div className="mis-pedidos-info-list">
        <Info icon={Route} label="Dirección de origen" value={pedido.direccionOrigen} />
        <Info icon={MapPin} label="Dirección de destino" value={pedido.direccionDestino} />
        <Info icon={Clock} label="Fecha de creación" value={formatFecha(pedido.fechaCreacion)} />
      </div>
    </div>
  );
}

function Info({ icon: Icon, label, value }) {
  return (
    <div className="mis-pedidos-info">
      {Icon && <Icon size={16} />}
      <span>
        <small>{label}</small>
        <strong>{value || "—"}</strong>
      </span>
    </div>
  );
}

function EstadoPagina({ titulo, texto, accion, error = false }) {
  return (
    <div className={`mis-pedidos-state ${error ? "error" : ""}`}>
      <h1>{titulo}</h1>
      <p>{texto}</p>
      {accion}
    </div>
  );
}

function normalizarError(err) {
  if (err?.status === 401) {
    return {
      titulo: "Sesión expirada",
      texto: "Iniciá sesión nuevamente para consultar tus pedidos.",
      reintentar: false,
    };
  }

  if (err?.status === 403) {
    return {
      titulo: "Acceso denegado",
      texto: "Tu usuario no tiene permiso para consultar el perfil de repartidor.",
      reintentar: false,
    };
  }

  if (err?.status === 404) {
    return {
      titulo: "Perfil operativo pendiente",
      texto: "Todavía no tenés un perfil de repartidor configurado. Un administrador debe crearlo antes de operar.",
      reintentar: true,
    };
  }

  return {
    titulo: "No pudimos cargar tus pedidos",
    texto: err?.message || "Ocurrió un error al consultar el backend.",
    reintentar: true,
  };
}

function nombreCompleto(perfil) {
  if (!perfil) return "—";
  return [perfil.nombre, perfil.apellido].filter(Boolean).join(" ") || "—";
}

function formatVehiculo(vehiculo) {
  const labels = {
    MOTO: "Moto",
    AUTO: "Auto / Utilitario",
    BICI: "Bicicleta",
  };
  return labels[vehiculo] || vehiculo || "—";
}

function formatResultado(resultado) {
  const labels = {
    ENTREGADO: "Entregado",
    CANCELADO: "Cancelado",
  };
  return labels[resultado] || resultado || "—";
}

function formatFecha(fecha) {
  if (!fecha) return "—";
  const parsed = new Date(fecha);
  if (Number.isNaN(parsed.getTime())) return fecha;
  return parsed.toLocaleString();
}
