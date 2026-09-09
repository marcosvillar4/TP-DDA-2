import { useAuthUser } from "../../hooks/useAuthUser";
import { DashboardWidget } from "./components/DashboardWidget";
import { DASHBOARD_WIDGETS_BY_ROLE } from "./dashboardConfig";
import "./styles/DashboardPage.css";

export default function DashboardPage() {
  const user = useAuthUser();
  const widgets = DASHBOARD_WIDGETS_BY_ROLE[user?.rol] ?? [];

  return (
    <div className="dashboard-page">
      <div className="dashboard-grid">
        {widgets.map((widget) => (
          <DashboardWidget
            key={widget.key}
            title={widget.title}
            description={widget.description}
            status={widget.status}
          />
        ))}
      </div>
    </div>
  );
}