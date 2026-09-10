import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Package, MapPin, Building2, Archive,
  Warehouse, Navigation, CreditCard, Bell, Truck, Users, Settings
} from 'lucide-react';
import logo from '../assets/Logo.png';
import './Sidebar.css';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard', roles: ['ADMIN', 'COMERCIO', 'REPARTIDOR', 'DEPOSITO'] },
  { to: '/pedidos', icon: Package, label: 'Pedidos', roles: ['ADMIN', 'COMERCIO', 'REPARTIDOR', 'DEPOSITO'] },
  { to: '/seguimiento', icon: MapPin, label: 'Seguimiento', roles: ['ADMIN', 'COMERCIO', 'REPARTIDOR', 'DEPOSITO'] },
  { to: '/comercios', icon: Building2, label: 'Comercios', roles: ['ADMIN'] },
  { to: '/inventario', icon: Archive, label: 'Inventario', roles: ['ADMIN', 'COMERCIO', 'DEPOSITO'] },
  { to: '/depositos', icon: Warehouse, label: 'Depósitos', roles: ['ADMIN'] },
  { to: '/repartidores', icon: Navigation, label: 'Repartidores', roles: ['ADMIN', 'DEPOSITO'] },
  { to: '/pagos', icon: CreditCard, label: 'Pagos y Cobranzas', roles: ['ADMIN', 'COMERCIO', 'REPARTIDOR'] },
  { to: '/alertas', icon: Bell, label: 'Alertas', roles: ['ADMIN', 'COMERCIO', 'REPARTIDOR', 'DEPOSITO'] },
  { to: '/transportistas', icon: Truck, label: 'Transportistas', roles: ['ADMIN', 'DEPOSITO'] },
  { to: '/usuarios', icon: Users, label: 'Usuarios', roles: ['ADMIN'] },
  { to: '/configuracion', icon: Settings, label: 'Configuración', roles: ['ADMIN', 'COMERCIO', 'REPARTIDOR', 'DEPOSITO'] },
];

export default function Sidebar() {
  let userRole = 'ADMIN';
  try {
    const raw = localStorage.getItem('logired_user');
    if (raw) {
      userRole = JSON.parse(raw).rol || 'ADMIN';
    }
  } catch {}

  const filteredNavItems = navItems.filter(item => !item.roles || item.roles.includes(userRole));

  return (
    <nav className="sidebar">
      <div className="sidebar-logo">
        <img src={logo} alt="LogiRed" className="sidebar-logo-img" />
      </div>
      <ul className="sidebar-nav">
        {filteredNavItems.map(({ to, icon: Icon, label }) => (
          <li key={to}>
            <NavLink
              to={to}
              className={({ isActive }) =>
                `sidebar-nav-item${isActive ? ' active' : ''}`
              }
            >
              <Icon size={18} className="sidebar-nav-icon" />
              <span>{label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
