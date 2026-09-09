import { useNavigate } from "react-router-dom";
import { LogOut } from "lucide-react";
import { ROLE_LABELS } from "../navConfig";
import "../styles/Topbar.css";

export function Topbar({ user, title }) {
  const navigate = useNavigate();

  function handleLogout() {
    localStorage.removeItem("logired_token");
    localStorage.removeItem("logired_user");
    navigate("/");
  }

  return (
    <header className="topbar">
      <h1 className="topbar-title">{title}</h1>

      <div className="topbar-user">
        <div className="topbar-user-info">
          <span className="topbar-user-name">{user?.username ?? "Usuario"}</span>
          <span className="topbar-user-role">
            {ROLE_LABELS[user?.rol] ?? user?.rol}
          </span>
        </div>
        <button
          type="button"
          className="topbar-logout"
          onClick={handleLogout}
          title="Cerrar sesión"
        >
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
}