import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { LogOut, User, Home, Settings } from "lucide-react";
import "./Dashboard.css";

export default function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const handleLogout = useCallback(() => {
    localStorage.removeItem("logired_token");
    localStorage.removeItem("logired_user");
    navigate("/");
  }, [navigate]);

  useEffect(() => {
    // Obtener datos del usuario desde localStorage
    const userStr = localStorage.getItem("logired_user");
    if (userStr) {
      try {
        const userData = JSON.parse(userStr);
        setUser(userData);
      } catch (err) {
        console.error("Error al parsear usuario:", err);
        handleLogout();
      }
    } else {
      // Si no hay usuario, redirigir al login
      navigate("/");
    }
    setLoading(false);
  }, [navigate, handleLogout]);

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="dashboard-spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="dashboard-container">
      {/* Navbar */}
      <nav className="dashboard-navbar">
        <div className="dashboard-navbar-content">
          <div className="dashboard-logo">
            <Home size={24} />
            <span>LogiRed</span>
          </div>
          <div className="dashboard-nav-right">
            <span className="dashboard-user-info">{user.username}</span>
            <button
              onClick={handleLogout}
              className="dashboard-logout-btn"
              title="Cerrar sesión"
            >
              <LogOut size={20} />
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="dashboard-main">
        {/* Sidebar */}
        <aside className="dashboard-sidebar">
          <div className="dashboard-sidebar-content">
            <div className="dashboard-user-card">
              <div className="dashboard-user-avatar">
                <User size={40} />
              </div>
              <h3 className="dashboard-user-name">{user.username}</h3>
              <p className="dashboard-user-role">{user.rol}</p>
            </div>

            <menu className="dashboard-menu">
              <li>
                <button className="dashboard-menu-item active" style={{ width: "100%", textAlign: "left", cursor: "pointer" }}>
                  <Home size={18} />
                  <span>Inicio</span>
                </button>
              </li>
              <li>
                <button className="dashboard-menu-item" style={{ width: "100%", textAlign: "left", cursor: "pointer" }}>
                  <User size={18} />
                  <span>Perfil</span>
                </button>
              </li>
              <li>
                <button className="dashboard-menu-item" style={{ width: "100%", textAlign: "left", cursor: "pointer" }}>
                  <Settings size={18} />
                  <span>Configuración</span>
                </button>
              </li>
            </menu>
          </div>
        </aside>

        {/* Content Area */}
        <main className="dashboard-content">
          <div className="dashboard-header">
            <h1>¡Bienvenido, {user.username}!</h1>
            <p className="dashboard-subtitle">
              Acceso como <strong>{user.rol}</strong>
            </p>
          </div>

          <div className="dashboard-grid">
            {/* Card de bienvenida */}
            <section className="dashboard-card">
              <h2 className="dashboard-card-title">Estado de tu cuenta</h2>
              <div className="dashboard-card-content">
                <div className="dashboard-info-item">
                  <label>ID de Usuario:</label>
                  <span>{user.id}</span>
                </div>
                <div className="dashboard-info-item">
                  <label>Nombre de usuario:</label>
                  <span>{user.username}</span>
                </div>
                <div className="dashboard-info-item">
                  <label>Rol:</label>
                  <span className="dashboard-role-badge">{user.rol}</span>
                </div>
              </div>
            </section>

            {/* Card de acciones rápidas */}
            <section className="dashboard-card">
              <h2 className="dashboard-card-title">Acciones disponibles</h2>
              <div className="dashboard-actions">
                <button className="dashboard-action-btn">
                  <span>📋</span>
                  <span>Ver pedidos</span>
                </button>
                <button className="dashboard-action-btn">
                  <span>📦</span>
                  <span>Inventario</span>
                </button>
                <button className="dashboard-action-btn">
                  <span>👥</span>
                  <span>Contactos</span>
                </button>
                <button className="dashboard-action-btn">
                  <span>📊</span>
                  <span>Reportes</span>
                </button>
              </div>
            </section>

            {/* Card de información */}
            <section className="dashboard-card dashboard-card-full">
              <h2 className="dashboard-card-title">Información de la plataforma</h2>
              <div className="dashboard-card-content">
                <p>
                  Estás conectado a <strong>LogiRed</strong>, la plataforma logística inteligente
                  que conecta comercios, depósitos y repartidores en tiempo real.
                </p>
                <p style={{ marginTop: "1rem", color: "#6b7280", fontSize: "0.875rem" }}>
                  Para más información o reportar un problema, contactá con nuestro equipo de soporte.
                </p>
              </div>
            </section>
          </div>
        </main>
      </div>
    </div>
  );
}


