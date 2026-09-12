import { useAuthUser } from "../../hooks/useAuthUser";
import { useDashboardMetrics } from "../../hooks/useDashboardMetrics";
import { DashboardWidget } from "./components/DashboardWidget";
import { DASHBOARD_WIDGETS_BY_ROLE } from "./dashboardConfig";
import "./styles/DashboardPage.css";

export default function DashboardPage() {
  const user = useAuthUser();
  const { metrics, loading } = useDashboardMetrics(user);
  const widgets = DASHBOARD_WIDGETS_BY_ROLE[user?.rol] ?? [];

  return (
    <div className="dashboard-page">
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
    </div>
  );
}