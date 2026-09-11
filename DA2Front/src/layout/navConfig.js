import {
  LayoutDashboard,
  ClipboardList,
  MapPin,
  Building2,
  Boxes,
  Warehouse,
  Navigation,
  CreditCard,
  Bell,
  Truck,
  Users,
  Package,
  Settings,
} from "lucide-react";

import DashboardPage from "../dashboard/DashboardPage";
import InventarioPage from "../inventario/InventarioPage";
import ComerciosList from "../comercios/ComerciosList";
import PedidosList from "../pedidos/PedidosList";
import SeguimientoPage from "../seguimiento/SeguimientoPage";

export const ROLES = {
  ADMIN: "ADMIN",
  COMERCIO: "COMERCIO",
  DEPOSITO: "DEPOSITO",
  REPARTIDOR: "REPARTIDOR",
};

const TODOS = [ROLES.ADMIN, ROLES.COMERCIO, ROLES.DEPOSITO, ROLES.REPARTIDOR];

/**
 * Fuente única de verdad del menú lateral y de las rutas de la app.
 *
 * - `roles`: quién ve el item en el Sidebar (y quién puede navegar a esa
 *   ruta, ya que la ruta se genera igual para todos en App.jsx).
 * - `element`: componente real de la pantalla. Si se omite, la ruta
 *   se renderiza automáticamente con <EnTrabajoPage/>.
 *
 * Para agregar una pantalla nueva más adelante: crearla y asignarla acá
 * en `element`. No hace falta tocar App.jsx ni el Sidebar.
 *
 * Las sub-rutas de detalle/alta (ej. /comercios/:id) NO van acá porque
 * no son ítems de menú — se agregan en App.jsx como EXTRA_ROUTES.
 */
export const NAV_ITEMS = [
  {
    key: "dashboard",
    label: "Dashboard",
    path: "/dashboard",
    icon: LayoutDashboard,
    roles: TODOS,
    element: DashboardPage,
  },
  {
    key: "pedidos",
    label: "Pedidos",
    path: "/pedidos",
    icon: ClipboardList,
    roles: [ROLES.ADMIN, ROLES.COMERCIO, ROLES.DEPOSITO],
    element: PedidosList,
  },
  {
    key: "mis-pedidos",
    label: "Mis Pedidos",
    path: "/mis-pedidos",
    icon: ClipboardList,
    roles: [ROLES.REPARTIDOR],
  },
  {
    key: "seguimiento",
    label: "Seguimiento",
    path: "/seguimiento",
    icon: MapPin,
    roles: TODOS,
    element: SeguimientoPage,
  },
  {
    key: "comercios",
    label: "Comercios",
    path: "/comercios",
    icon: Building2,
    roles: [ROLES.ADMIN],
    element: ComerciosList,
  },
  {
    key: "inventario",
    label: "Inventario",
    path: "/inventario",
    icon: Boxes,
    roles: [ROLES.ADMIN, ROLES.COMERCIO, ROLES.DEPOSITO],
    element: InventarioPage,
  },
  {
    key: "productos",
    label: "Productos",
    path: "/productos",
    icon: Package,
    roles: [ROLES.ADMIN, ROLES.COMERCIO],
  },
  {
    key: "depositos",
    label: "Depósitos",
    path: "/depositos",
    icon: Warehouse,
    roles: [ROLES.ADMIN],
  },
  {
    key: "repartidores",
    label: "Repartidores",
    path: "/repartidores",
    icon: Navigation,
    roles: [ROLES.ADMIN],
  },
  {
    key: "transportistas",
    label: "Transportistas",
    path: "/transportistas",
    icon: Truck,
    roles: [ROLES.ADMIN],
  },
  {
    key: "pagos",
    label: "Pagos y Cobranzas",
    path: "/pagos",
    icon: CreditCard,
    roles: [ROLES.ADMIN, ROLES.COMERCIO, ROLES.DEPOSITO],
  },
  {
    key: "alertas",
    label: "Alertas",
    path: "/alertas",
    icon: Bell,
    roles: TODOS,
  },
  {
    key: "usuarios",
    label: "Usuarios",
    path: "/usuarios",
    icon: Users,
    roles: [ROLES.ADMIN],
  },
  {
    key: "configuracion",
    label: "Configuración",
    path: "/configuracion",
    icon: Settings,
    roles: TODOS,
  },
];

export const ROLE_LABELS = {
  ADMIN: "Administrador",
  COMERCIO: "Comercio",
  DEPOSITO: "Depósito",
  REPARTIDOR: "Repartidor",
};

export function getNavItemsForRole(rol) {
  return NAV_ITEMS.filter((item) => item.roles.includes(rol));
}