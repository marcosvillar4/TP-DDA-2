import "../styles/DashboardWidget.css";

const STATUS_CLASS = {
  PRONTO: "dashboard-widget-badge-pronto",
  "EN TRABAJO": "dashboard-widget-badge-trabajo",
};

export function DashboardWidget({ title, description, status = "PRONTO", value, loading }) {
  const activado = value !== undefined && value !== null;

  return (
    <div className="dashboard-widget">
      <div className="dashboard-widget-header">
        <h3 className="dashboard-widget-title">{title}</h3>
        {!activado && (
          <span className={`dashboard-widget-badge ${STATUS_CLASS[status] ?? ""}`}>
            {status}
          </span>
        )}
      </div>

      {activado && (
        <p className="dashboard-widget-value">{loading ? "…" : value}</p>
      )}

      <p className="dashboard-widget-description">{description}</p>
    </div>
  );
}