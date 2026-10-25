import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, LogOut } from "lucide-react";
import { getCantidadNotificacionesNoLeidas } from "../../../api/notificacionesApi";
import { ROLE_LABELS } from "../navConfig";
import "../styles/Topbar.css";

export function Topbar({ user, title }) {
  const navigate = useNavigate();
  const [noLeidas, setNoLeidas] = useState(0);
  const nombreCompleto = [user?.nombre, user?.apellido].filter(Boolean).join(" ") || "Usuario";

  useEffect(() => {
    let activo = true;

    const cargarCantidad = async () => {
      try {
        const cantidad = await getCantidadNotificacionesNoLeidas();
        if (activo) setNoLeidas(cantidad);
      } catch {
        // La campana no debe impedir el uso del resto de la interfaz.
      }
    };

    cargarCantidad();
    const intervalo = window.setInterval(cargarCantidad, 30000);
    window.addEventListener("notificaciones:actualizadas", cargarCantidad);

    return () => {
      activo = false;
      window.clearInterval(intervalo);
      window.removeEventListener("notificaciones:actualizadas", cargarCantidad);
    };
  }, []);

  function handleLogout() {
    localStorage.removeItem("logired_token");
    localStorage.removeItem("logired_user");
    navigate("/");
  }

  return (
    <header className="topbar">
      <h1 className="topbar-title">{title}</h1>

      <div className="topbar-user">
        <button
          type="button"
          className="topbar-notifications"
          onClick={() => navigate("/alertas")}
          title="Ver alertas"
          aria-label={`Ver alertas${noLeidas ? `, ${noLeidas} sin leer` : ""}`}
        >
          <Bell size={19} />
          {noLeidas > 0 && (
            <span className="topbar-notifications-badge">
              {noLeidas > 99 ? "99+" : noLeidas}
            </span>
          )}
        </button>
        <div className="topbar-user-info">
          <span className="topbar-user-name">{nombreCompleto}</span>
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