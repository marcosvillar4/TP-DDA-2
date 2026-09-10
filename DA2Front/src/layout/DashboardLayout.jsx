import { useState } from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuthUser } from "../../hooks/useAuthUser";
import { Sidebar } from "./components/Sidebar";
import { Topbar } from "./components/Topbar";
import { getNavItemsForRole } from "./navConfig";
import "./styles/DashboardLayout.css";

export default function DashboardLayout() {
  const user = useAuthUser();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  if (!user) {
    return <Navigate to="/" replace />;
  }

  const items = getNavItemsForRole(user.rol);
  const activeItem = items.find((item) => item.path === location.pathname);
  const title = activeItem?.label ?? "LogiRed";

  return (
    <div className="dashboard-layout">
      <Sidebar
        items={items}
        collapsed={collapsed}
        onToggleCollapse={() => setCollapsed((prev) => !prev)}
      />

      <div className="dashboard-layout-main">
        <Topbar user={user} title={title} />
        <div className="dashboard-layout-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}