import { useLocation, useNavigate } from 'react-router-dom';
import { Bell } from 'lucide-react';
import './Header.css';

const routeTitles = {
  '/dashboard': 'Dashboard',
  '/pedidos': 'Pedidos',
  '/seguimiento': 'Seguimiento',
  '/comercios': 'Comercios',
  '/inventario': 'Inventario',
  '/depositos': 'Depósitos',
  '/repartidores': 'Repartidores',
  '/pagos': 'Pagos y Cobranzas',
  '/alertas': 'Alertas',
  '/transportistas': 'Transportistas',
  '/usuarios': 'Usuarios',
  '/configuracion': 'Configuración',
};

const rolLabels = {
  ADMIN: 'Administrador',
  COMERCIO: 'Comercio',
  REPARTIDOR: 'Repartidor',
  DEPOSITO: 'Depósito',
};

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const title = routeTitles[location.pathname] ?? 'LogiRed';

  let username = 'Admin';
  let rol = 'Administrador';
  let inicial = 'A';
  try {
    const raw = localStorage.getItem('logired_user');
    if (raw) {
      const u = JSON.parse(raw);
      username = u.username ?? 'Admin';
      rol = rolLabels[u.rol] ?? u.rol ?? 'Administrador';
      inicial = username.charAt(0).toUpperCase();
    }
  } catch {}

  function handleLogout() {
    localStorage.removeItem('logired_token');
    localStorage.removeItem('logired_user');
    navigate('/');
  }

  return (
    <header className="app-header">
      <h1 className="app-header-title">{title}</h1>
      <div className="app-header-actions">
        <button className="app-header-bell" aria-label="Notificaciones">
          <Bell size={20} />
          <span className="app-header-bell-badge">2</span>
        </button>
        <div className="app-header-user">
          <div className="app-header-avatar">{inicial}</div>
          <div className="app-header-user-info">
            <span className="app-header-user-name">{username}</span>
            <span className="app-header-user-role">{rol}</span>
          </div>
          <button className="app-header-logout" onClick={handleLogout}>
            Salir
          </button>
        </div>
      </div>
    </header>
  );
}
