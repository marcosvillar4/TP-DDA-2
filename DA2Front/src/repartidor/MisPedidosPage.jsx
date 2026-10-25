import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  CheckCircle2,
  Clock,
  MapPin,
  PackageCheck,
  PackageSearch,
  Play,
  RefreshCw,
  Route,
  Truck,
} from "lucide-react";
import Swal from "sweetalert2";
import {
  entregarPedidoActual,
  getMiHistorialRepartidor,
  getMiPerfilRepartidor,
  getMisPedidosDisponibles,
  iniciarViajePedido,
  retirarPedidoAsignado,
  tomarPedidoDisponible,
} from "../../api/repartidorPerfilApi";
import BadgeEstado from "../pedidos/components/BadgeEstado";
import RepartidorEstadoBadge from "../repartidores/components/RepartidorEstadoBadge";
import "./styles/MisPedidosPage.css";

export default function MisPedidosPage() {
  const [perfil, setPerfil] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [pedidosDisponibles, setPedidosDisponibles] = useState([]);
  const [pedidoActualDetalle, setPedidoActualDetalle] = useState(null);
  const [loadingPerfil, setLoadingPerfil] = useState(true);
  const [loadingHistorial, setLoadingHistorial] = useState(true);
  const [loadingDisponibles, setLoadingDisponibles] = useState(false);
  const [errorPerfil, setErrorPerfil] = useState(null);
  const [errorHistorial, setErrorHistorial] = useState(null);
  const [errorDisponibles, setErrorDisponibles] = useState(null);
  const [errorOperacion, setErrorOperacion] = useState(null);
  const [operacionActiva, setOperacionActiva] = useState(null);
  const [mensajeExito, setMensajeExito] = useState(null);
  const mountedRef = useRef(false);
  const operacionEnCursoRef = useRef(false);

  const cargarPerfil = useCallback(async () => {
    setLoadingPerfil(true);
    setErrorPerfil(null);

    try {
      const perfilData = await getMiPerfilRepartidor();
      if (!mountedRef.current) return;
      setPerfil(perfilData);
      if (!perfilData?.pedidoActual) {
        setPedidoActualDetalle(null);
      }
    } catch (err) {
      if (!mountedRef.current) return;
      setPerfil(null);
      setPedidoActualDetalle(null);
      setErrorPerfil(normalizarError(err, "perfil"));
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
      setErrorHistorial(normalizarError(err, "historial"));
    } finally {
      if (mountedRef.current) {
        setLoadingHistorial(false);
      }
    }
  }, []);

  const cargarDisponibles = useCallback(async () => {
    setLoadingDisponibles(true);
    setErrorDisponibles(null);

    try {
      const disponiblesData = await getMisPedidosDisponibles();
      if (!mountedRef.current) return;
      setPedidosDisponibles(Array.isArray(disponiblesData) ? disponiblesData : []);
    } catch (err) {
      if (!mountedRef.current) return;
      setPedidosDisponibles([]);
      setErrorDisponibles(normalizarError(err, "disponibles"));
    } finally {
      if (mountedRef.current) {
        setLoadingDisponibles(false);
      }
    }
  }, []);

  useEffect(() => {
    mountedRef.current = true;
    const timeoutId = window.setTimeout(() => {
      cargarPerfil();
      cargarHistorial();
    }, 0);

    return () => {
      window.clearTimeout(timeoutId);
      mountedRef.current = false;
    };
  }, [cargarHistorial, cargarPerfil]);

  useEffect(() => {
    if (!perfil) return;

    const timeoutId = window.setTimeout(() => {
      if (!mountedRef.current) return;

      if (puedeConsultarDisponibles(perfil)) {
        cargarDisponibles();
        return;
      }

      setPedidosDisponibles([]);
      setErrorDisponibles(null);
      setLoadingDisponibles(false);
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [cargarDisponibles, perfil]);

  const pedidoActual = combinarPedidoActual(perfil?.pedidoActual, pedidoActualDetalle);

  async function ejecutarOperacion({ tipo, pedido, titulo, texto, confirmarTexto, api, exito }) {
    const pedidoId = pedido?.id;
    if (!pedidoId || operacionEnCursoRef.current) return;

    operacionEnCursoRef.current = true;
    setOperacionActiva(`${tipo}-${pedidoId}`);
    try {
      const confirmacion = await Swal.fire({
        icon: "question",
        title: titulo,
        text: texto,
        showCancelButton: true,
        confirmButtonText: confirmarTexto,
        cancelButtonText: "Cancelar",
        confirmButtonColor: "#16223f",
        cancelButtonColor: "#64748b",
      });

      if (!confirmacion.isConfirmed || !mountedRef.current) return;

      setErrorOperacion(null);
      setMensajeExito(null);

      const pedidoActualizado = await api(pedidoId);
      if (!mountedRef.current) return;

      if (tipo === "entregar") {
        setPedidoActualDetalle(null);
      } else {
        setPedidoActualDetalle(pedidoActualizado);
      }

      await Promise.all([
        cargarPerfil(),
        tipo === "entregar" ? cargarHistorial() : Promise.resolve(),
      ]);

      if (!mountedRef.current) return;
      setMensajeExito(exito);
      setPedidosDisponibles((prev) => prev.filter((item) => item.id !== pedidoId));

      await Swal.fire({
        icon: "success",
        title: "Listo",
        text: exito,
        confirmButtonColor: "#16223f",
      });
    } catch (err) {
      if (!mountedRef.current) return;
      const error = normalizarError(err, "operacion");
      setErrorOperacion(error);
      if (err?.status === 409) {
        // El cambio de perfil refresca o limpia disponibles mediante el efecto existente.
        await cargarPerfil();
        if (!mountedRef.current) return;
      }
      await Swal.fire({
        icon: "error",
        title: error.titulo,
        text: error.texto,
        confirmButtonColor: "#16223f",
      });
    } finally {
      operacionEnCursoRef.current = false;
      if (mountedRef.current) {
        setOperacionActiva(null);
      }
    }
  }

  function tomarPedido(pedido) {
    return ejecutarOperacion({
      tipo: "tomar",
      pedido,
      titulo: `Tomar pedido #${pedido.id}`,
      texto: "Vas a quedar asignado a este pedido y pasar a estado En entrega.",
      confirmarTexto: "Tomar pedido",
      api: tomarPedidoDisponible,
      exito: `Pedido #${pedido.id} tomado correctamente.`,
    });
  }

  function operarPedidoActual(accion) {
    if (!pedidoActual) return Promise.resolve();

    const config = ACCIONES_PEDIDO[pedidoActual.estado];
    if (!config || config.accion !== accion) {
      setErrorOperacion({
        titulo: "Operación no disponible",
        texto: "La acción no corresponde al estado actual del pedido. Actualizá la pantalla.",
      });
      return Promise.resolve();
    }
    return ejecutarOperacion({
      tipo: config.accion,
      pedido: pedidoActual,
      titulo: config.titulo(pedidoActual),
      texto: config.texto,
      confirmarTexto: config.confirmarTexto,
      api: config.api,
      exito: config.exito(pedidoActual),
    });
  }

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

  return (
    <div className="mis-pedidos-page">
      <header className="mis-pedidos-header">
        <div>
          <span className="mis-pedidos-eyebrow">Repartidor</span>
          <h1>Mis Pedidos</h1>
          <p>Consultá pedidos disponibles, gestioná tu entrega actual y revisá tu historial.</p>
        </div>
        {perfil && <RepartidorEstadoBadge estado={perfil.estado} activo={perfil.activo} />}
      </header>

      {mensajeExito && (
        <div className="mis-pedidos-feedback success" role="status">
          <CheckCircle2 size={18} />
          <span>{mensajeExito}</span>
        </div>
      )}

      {errorOperacion && (
        <div className="mis-pedidos-feedback error" role="alert">
          <strong>{errorOperacion.titulo}</strong>
          <span>{errorOperacion.texto}</span>
        </div>
      )}

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
            <PedidoActual
              pedido={pedidoActual}
              operacionActiva={operacionActiva}
              onAccion={operarPedidoActual}
            />
          ) : (
            <div className="mis-pedidos-empty">
              <PackageCheck size={24} />
              <strong>No tenés pedidos asignados en este momento</strong>
              <span>Si estás disponible, podés tomar uno de los pedidos listos para retirar.</span>
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
            <h2>Pedidos disponibles</h2>
            <p>Pedidos listos para retirar y sin repartidor asignado.</p>
          </div>
          <button
            className="mis-pedidos-btn secondary"
            onClick={cargarDisponibles}
            type="button"
            disabled={!puedeConsultarDisponibles(perfil) || loadingDisponibles || Boolean(operacionActiva)}
          >
            <RefreshCw size={16} />
            Actualizar
          </button>
        </div>

        {!puedeConsultarDisponibles(perfil) ? (
          <div className="mis-pedidos-empty compact">
            <PackageSearch size={24} />
            <strong>No podés consultar pedidos disponibles ahora</strong>
            <span>
              El listado se habilita cuando tu perfil está activo, disponible y sin pedido actual.
            </span>
          </div>
        ) : errorDisponibles ? (
          <div className="mis-pedidos-section-error" role="alert">
            <strong>{errorDisponibles.titulo}</strong>
            <span>{errorDisponibles.texto}</span>
            {errorDisponibles.reintentar && (
              <button className="mis-pedidos-btn" onClick={cargarDisponibles} type="button">
                <RefreshCw size={16} />
                Reintentar disponibles
              </button>
            )}
          </div>
        ) : loadingDisponibles ? (
          <div className="mis-pedidos-table-empty">Cargando pedidos disponibles...</div>
        ) : pedidosDisponibles.length === 0 ? (
          <div className="mis-pedidos-empty compact">
            <PackageCheck size={24} />
            <strong>No hay pedidos disponibles</strong>
            <span>Cuando un pedido quede listo para retirar, va a aparecer en esta sección.</span>
          </div>
        ) : (
          <div className="mis-pedidos-available-list">
            {pedidosDisponibles.map((pedido) => (
              <PedidoDisponible
                key={pedido.id}
                pedido={pedido}
                disabled={Boolean(operacionActiva)}
                loading={operacionActiva === `tomar-${pedido.id}`}
                onTomar={() => tomarPedido(pedido)}
              />
            ))}
          </div>
        )}
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
                  <th>Fecha de finalización</th>
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
                    <td>{item.fecha && !Number.isNaN(new Date(item.fecha).getTime())
                      ? formatFecha(item.fecha)
                      : "No disponible"}</td>
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

const ACCIONES_PEDIDO = {
  ASIGNADO: {
    accion: "retirar",
    label: "Confirmar retiro",
    icon: PackageCheck,
    api: retirarPedidoAsignado,
    titulo: (pedido) => `Confirmar retiro del pedido #${pedido.id}`,
    texto: "Confirmá esta acción únicamente cuando ya hayas retirado el pedido.",
    confirmarTexto: "Confirmar retiro",
    exito: (pedido) => `Retiro confirmado para el pedido #${pedido.id}.`,
  },
  RETIRADO: {
    accion: "iniciar-viaje",
    label: "Iniciar viaje",
    icon: Play,
    api: iniciarViajePedido,
    titulo: (pedido) => `Iniciar viaje del pedido #${pedido.id}`,
    texto: "El pedido pasará a estado En camino.",
    confirmarTexto: "Iniciar viaje",
    exito: (pedido) => `Viaje iniciado para el pedido #${pedido.id}.`,
  },
  EN_CAMINO: {
    accion: "entregar",
    label: "Confirmar entrega",
    icon: CheckCircle2,
    api: entregarPedidoActual,
    titulo: (pedido) => `Confirmar entrega del pedido #${pedido.id}`,
    texto: "El pedido se marcará como entregado y volverás a estar disponible.",
    confirmarTexto: "Confirmar entrega",
    exito: (pedido) => `Pedido #${pedido.id} entregado correctamente.`,
  },
};

function PedidoActual({ pedido, operacionActiva, onAccion }) {
  const navigate = useNavigate();
  const accion = ACCIONES_PEDIDO[pedido.estado];
  const Icon = accion?.icon;
  const loading = accion ? operacionActiva === `${accion.accion}-${pedido.id}` : false;

  return (
    <div className="mis-pedidos-current-body">
      <div className="mis-pedidos-current-main">
        <span className="mis-pedidos-id">#{pedido.id}</span>
        <BadgeEstado estado={pedido.estado} />
        <button
          type="button"
          className="mis-pedidos-btn"
          style={{ marginLeft: "auto" }}
          onClick={() => navigate(`/pedidos/${pedido.id}`)}
        >
          Ver detalle del pedido
        </button>
      </div>

      <div className="mis-pedidos-info-list">
        <Info label="Depósito" value={pedido.depositoNombre || "Nombre no disponible"} />
        <Info label="ID del depósito" value={pedido.depositoId != null ? `#${pedido.depositoId}` : "ID no disponible"} />
        <Info icon={MapPin} label="Dirección del depósito" value={pedido.depositoDireccion || "Dirección no disponible"} />
        <Info icon={Route} label="Dirección de origen" value={pedido.direccionOrigen} />
        <Info icon={MapPin} label="Dirección de destino" value={pedido.direccionDestino} />
        <Info icon={Clock} label="Fecha de creación" value={formatFecha(pedido.fechaCreacion)} />
      </div>

      {Array.isArray(pedido.detalles) && pedido.detalles.length > 0 && (
        <div className="mis-pedidos-detalles">
          <small>Productos</small>
          {pedido.detalles.map((detalle) => (
            <span key={detalle.id ?? `${detalle.productoId}-${detalle.productoNombre}`}>
              {detalle.productoNombre || `Producto #${detalle.productoId}`} · {detalle.cantidad ?? "—"} unidad
              {detalle.cantidad === 1 ? "" : "es"}
            </span>
          ))}
        </div>
      )}

      {accion ? (
        <button
          className="mis-pedidos-btn action"
          onClick={() => onAccion(accion.accion)}
          type="button"
          disabled={Boolean(operacionActiva)}
        >
          {loading ? <RefreshCw size={16} className="mis-pedidos-spin" /> : <Icon size={16} />}
          {loading ? "Procesando..." : accion.label}
        </button>
      ) : (
        <p className="mis-pedidos-note">
          No hay acciones disponibles para el estado actual del pedido.
        </p>
      )}
    </div>
  );
}

function PedidoDisponible({ pedido, disabled, loading, onTomar }) {
  return (
    <article className="mis-pedidos-available-item">
      <div className="mis-pedidos-available-main">
        <div>
          <span className="mis-pedidos-id">#{pedido.id}</span>
          <h3>{pedido.direccionDestino || "Destino sin informar"}</h3>
        </div>
        <BadgeEstado estado={pedido.estado} />
      </div>

      <div className="mis-pedidos-available-info">
        <Info icon={Route} label="Origen" value={pedido.direccionOrigen} />
        <Info icon={Clock} label="Creado" value={formatFecha(pedido.fechaCreacion)} />
        <Info label="Depósito" value={pedido.depositoId ? `#${pedido.depositoId}` : null} />
      </div>

      {Array.isArray(pedido.detalles) && pedido.detalles.length > 0 && (
        <div className="mis-pedidos-detalles">
          <small>Productos</small>
          {pedido.detalles.map((detalle) => (
            <span key={detalle.id ?? `${detalle.productoId}-${detalle.productoNombre}`}>
              {detalle.productoNombre || `Producto #${detalle.productoId}`} · {detalle.cantidad ?? "—"} unidad
              {detalle.cantidad === 1 ? "" : "es"}
            </span>
          ))}
        </div>
      )}

      <button
        className="mis-pedidos-btn"
        onClick={onTomar}
        type="button"
        disabled={disabled}
      >
        {loading ? <RefreshCw size={16} className="mis-pedidos-spin" /> : <Truck size={16} />}
        {loading ? "Tomando..." : "Tomar pedido"}
      </button>
    </article>
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

function normalizarError(err, contexto) {
  const titulos = {
    perfil: "No pudimos cargar tu perfil",
    historial: "No pudimos cargar tu historial",
    disponibles: "No pudimos cargar los pedidos disponibles",
    operacion: "No pudimos actualizar el pedido",
  };
  const permisos = {
    perfil: "Tu usuario no tiene permiso para consultar el perfil de repartidor.",
    historial: "No tenés permiso para consultar este historial de pedidos.",
    disponibles: "No tenés permiso para consultar pedidos disponibles.",
    operacion: "No tenés permiso para realizar esta operación sobre el pedido.",
  };
  const mensaje = typeof err?.message === "string" ? err.message.trim() : "";
  const detalle = mensaje === "Ocurrió un error inesperado. Intentá nuevamente." ? "" : mensaje;

  if (err?.status === 400) {
    return {
      titulo: "Solicitud inválida",
      texto: detalle || (contexto === "operacion"
        ? "No se pudo procesar la operación. Revisá los datos del pedido e intentá nuevamente."
        : "No se pudo procesar la consulta. Actualizá la información e intentá nuevamente."),
      reintentar: true,
    };
  }

  if (err?.status === 401) {
    return {
      titulo: "Sesión expirada",
      texto: "Iniciá sesión nuevamente para continuar.",
      reintentar: false,
    };
  }

  if (err?.status === 403) {
    return {
      titulo: "Acceso denegado",
      texto: detalle || permisos[contexto],
      reintentar: false,
    };
  }

  if (err?.status === 404) {
    return {
      titulo: contexto === "perfil" ? "Perfil operativo pendiente"
        : contexto === "operacion" ? "Pedido no encontrado" : "Información no encontrada",
      texto: detalle || (contexto === "perfil"
        ? "Todavía no tenés un perfil de repartidor configurado. Un administrador debe crearlo antes de operar."
        : contexto === "operacion"
          ? "El pedido ya no existe o no está disponible para esta operación."
          : "No se encontró la información solicitada. Actualizá la consulta e intentá nuevamente."),
      reintentar: true,
    };
  }

  if (err?.status === 409) {
    return {
      titulo: contexto === "operacion" ? "Operación no disponible" : "Consulta no disponible",
      texto: detalle || (contexto === "operacion"
        ? "El pedido o tu disponibilidad cambiaron de estado. Revisá la información antes de reintentar."
        : "El estado actual no permite completar esta consulta."),
      reintentar: true,
    };
  }

  return {
    titulo: titulos[contexto],
    texto: detalle || (contexto === "operacion"
      ? "No se pudo completar la operación. Intentá nuevamente."
      : "No se pudo consultar el servidor. Intentá nuevamente."),
    reintentar: true,
  };
}

function puedeConsultarDisponibles(perfil) {
  return Boolean(perfil?.activo && perfil?.estado === "DISPONIBLE" && !perfil?.pedidoActual);
}

function combinarPedidoActual(pedidoPerfil, pedidoDetalle) {
  if (!pedidoPerfil) return null;
  if (!pedidoDetalle || pedidoDetalle.id !== pedidoPerfil.id) return pedidoPerfil;

  return {
    ...pedidoDetalle,
    ...pedidoPerfil,
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
