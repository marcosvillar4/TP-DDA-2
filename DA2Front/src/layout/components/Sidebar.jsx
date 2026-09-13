import { NavLink } from "react-router-dom";
import "../styles/Sidebar.css";

export function Sidebar({ items, collapsed, onToggleCollapse }) {
  return (
    <aside className={`sidebar ${collapsed ? "sidebar-collapsed" : ""}`}>
      <div className="sidebar-brand">
        {collapsed ? (
          <span className="sidebar-brand-mark sidebar-brand-mini" title="LogiRed">
            L<span className="text-red">R</span>
          </span>
        ) : (
          <span className="sidebar-brand-mark">
            Logi<span className="text-red">Red</span>
          </span>
        )}
      </div>

      <nav className="sidebar-nav">
        {items.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.key}
              to={item.path}
              className={({ isActive }) =>
                `sidebar-nav-item ${isActive ? "sidebar-nav-item-active" : ""}`
              }
            >
              <Icon size={18} />
              {!collapsed && <span>{item.label}</span>}
            </NavLink>
          );
        })}
      </nav>

      <button
        type="button"
        className="sidebar-collapse-toggle"
        onClick={onToggleCollapse}
      >
        {collapsed ? "»" : "« Contraer menú"}
      </button>
    </aside>
  );
}