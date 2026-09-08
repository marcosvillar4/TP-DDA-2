import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Package, MapPin, Building2, Archive,
  Warehouse, Navigation, CreditCard, Bell, Truck, Users, Settings
} from 'lucide-react';
import logo from '../assets/Logo.png';
import './Sidebar.css';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/pedidos', icon: Package, label: 'Pedidos' },
  { to: '/seguimiento', icon: MapPin, label: 'Seguimiento' },
  { to: '/comercios', icon: Building2, label: 'Comercios' },
  { to: '/inventario', icon: Archive, label: 'Inventario' },
  { to: '/depositos', icon: Warehouse, label: 'Depósitos' },
  { to: '/repartidores', icon: Navigation, label: 'Repartidores' },
  { to: '/pagos', icon: CreditCard, label: 'Pagos y Cobranzas' },
  { to: '/alertas', icon: Bell, label: 'Alertas' },
  { to: '/transportistas', icon: Truck, label: 'Transportistas' },
  { to: '/usuarios', icon: Users, label: 'Usuarios' },
  { to: '/configuracion', icon: Settings, label: 'Configuración' },
];

export default function Sidebar() {
  return (
    <nav className="sidebar">
      <div className="sidebar-logo">
        <img src={logo} alt="LogiRed" className="sidebar-logo-img" />
      </div>
      <ul className="sidebar-nav">
        {navItems.map(({ to, icon: Icon, label }) => (
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
