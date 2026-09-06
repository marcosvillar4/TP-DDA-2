import { useNavigate } from "react-router-dom";
import "../styles/InventarioTopBar.css";

export function InventarioTopBar() {
  const navigate = useNavigate();

  function handleLogout() {
    localStorage.removeItem("logired_token");
    localStorage.removeItem("logired_user");
    navigate("/");
  }

  return (
    <header className="inventario-topbar">
      <div className="inventario-topbar-brand">
        <span className="inventario-topbar-brand-mark">LogiRed</span>
        <h1 className="inventario-topbar-title">Gestión de Inventario</h1>
      </div>
      <button
        type="button"
        onClick={handleLogout}
        className="inventario-topbar-logout"
      >
        Cerrar sesión
      </button>
    </header>
  );
}