import { useAuthUser } from "../../hooks/useAuthUser";
import { useDashboardMetrics } from "../../hooks/useDashboardMetrics";
import { DashboardWidget } from "./components/DashboardWidget";
import EstadoPedidos from "./components/EstadoPedidos";
import PedidosRecientes from "./components/PedidosRecientes";
import RepartidoresPanel from "./components/RepartidoresPanel";
import AlertasOperativas from "./components/AlertasOperativas";
import HistorialEntregas from "./components/HistorialEntregas";
import { DASHBOARD_WIDGETS_BY_ROLE } from "./dashboardConfig";
import "./styles/DashboardPage.css";

/** Paneles inferiores según el rol; todos reciben datos del backend. */
function Paneles({ rol, metrics, data }) {
  if (rol === "ADMIN") {
    return (
      <>
        <EstadoPedidos conteo={metrics.pedidosPorEstado} alcance="en el sistema" />
        <RepartidoresPanel repartidores={data.repartidores} />
        <PedidosRecientes pedidos={data.pedidos} mostrarComercio verTodosPath="/pedidos" />
        <AlertasOperativas alertas={data.alertas} />
      </>
    );
  }

  if (rol === "COMERCIO") {
    return (
      <>
        <EstadoPedidos conteo={metrics.pedidosPorEstado} alcance="de tu comercio" />
        <AlertasOperativas alertas={data.alertas} />
        <PedidosRecientes pedidos={data.pedidos} verTodosPath="/pedidos" />
      </>
    );
  }

  if (rol === "DEPOSITO") {
    if (data.sinDeposito) return null;
    return (
      <>
        <EstadoPedidos conteo={metrics.pedidosPorEstado} alcance="del comercio vinculado" />
        <AlertasOperativas alertas={data.alertas} />
        <PedidosRecientes pedidos={data.pedidos} verTodosPath="/pedidos" />
      </>
    );
  }

  if (rol === "REPARTIDOR") {
    if (data.sinPerfil) return null;
    return <HistorialEntregas historial={data.historial} />;
  }

  return null;
}

export default function DashboardPage() {
  const user = useAuthUser();
  const { metrics, data, errores, loading } = useDashboardMetrics(user);
  const widgets = DASHBOARD_WIDGETS_BY_ROLE[user?.rol] ?? [];

  return (
    <div className="dashboard-page">
      {!loading && errores.length > 0 && (
        <div className="dashboard-aviso" role="alert">
          No se pudo cargar: {errores.join(", ")}. Los datos mostrados pueden estar incompletos.
        </div>
      )}

      <div className="dashboard-grid">
        {widgets.map((widget) => {
          const value = widget.metric ? widget.metric(metrics) : undefined;
          const description =
            value !== undefined && value !== null && widget.metricDescription
              ? widget.metricDescription(metrics)
              : widget.description;

          return (
            <DashboardWidget
              key={widget.key}
              title={widget.title}
              description={description}
              status={widget.status}
              value={value}
              loading={loading && widget.metric !== undefined}
            />
          );
        })}
      </div>

      {!loading && (
        <div className="dashboard-panels">
          <Paneles rol={user?.rol} metrics={metrics} data={data} />
        </div>
      )}
    </div>
  );
}
