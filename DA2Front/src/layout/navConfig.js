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
  Store,
} from "lucide-react";

import DashboardPage from "../dashboard/DashboardPage";
import InventarioPage from "../inventario/InventarioPage";
import ComerciosList from "../comercios/ComerciosList";
import MiComercio from "../comercios/MiComercio";
import PedidosList from "../pedidos/PedidosList";
import SeguimientoPage from "../seguimiento/SeguimientoPage";
import ProductosPage from "../productos/ProductosPage";
import RepartidoresPage from "../repartidores/RepartidoresPage";
import MisPedidosPage from "../repartidor/MisPedidosPage";
import UsuariosPage from "../usuarios/UsuariosPage";
import DepositosPage from "../depositos/DepositosPage";
import MisDepositosPage from "../depositos/MisDepositosPage";
import AlertasPage from "../alertas/AlertasPage";

import { PERMISOS_RUTA } from "./routePermissions";
export { ROLES } from "./routePermissions";

/**
 * Fuente única de verdad del menú lateral y de las rutas de la app.
 *
 * - `roles`: permisos de routePermissions, compartidos por Sidebar y RoleGuard.
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
    roles: PERMISOS_RUTA["/dashboard"],
    element: DashboardPage,
  },
  {
    key: "pedidos",
    label: "Pedidos",
    path: "/pedidos",
    icon: ClipboardList,
    roles: PERMISOS_RUTA["/pedidos"],
    element: PedidosList,
  },
  {
    key: "mis-pedidos",
    label: "Mis Pedidos",
    path: "/mis-pedidos",
    icon: ClipboardList,
    roles: PERMISOS_RUTA["/mis-pedidos"],
    element: MisPedidosPage,
  },
  {
    key: "seguimiento",
    label: "Seguimiento",
    path: "/seguimiento",
    icon: MapPin,
    roles: PERMISOS_RUTA["/seguimiento"],
    element: SeguimientoPage,
  },
  {
    key: "comercios",
    label: "Comercios",
    path: "/comercios",
    icon: Building2,
    roles: PERMISOS_RUTA["/comercios"],
    element: ComerciosList,
  },
  {
    key: "mi-comercio",
    label: "Mi Comercio",
    path: "/mi-comercio",
    icon: Store,
    roles: PERMISOS_RUTA["/mi-comercio"],
    element: MiComercio,
  },
  {
    key: "productos",
    label: "Productos",
    path: "/productos",
    icon: Package,
    roles: PERMISOS_RUTA["/productos"],
    element: ProductosPage,
  },
  {
    key: "inventario",
    label: "Inventario",
    path: "/inventario",
    icon: Boxes,
    roles: PERMISOS_RUTA["/inventario"],
    element: InventarioPage,
  },
  {
    key: "depositos",
    label: "Depósitos",
    path: "/depositos",
    icon: Warehouse,
    roles: PERMISOS_RUTA["/depositos"],
    element: DepositosPage,
  },
  {
    key: "mis-depositos",
    label: "Mis Depósitos",
    path: "/mis-depositos",
    icon: Warehouse,
    roles: PERMISOS_RUTA["/mis-depositos"],
    element: MisDepositosPage,
  },
  {
    key: "repartidores",
    label: "Repartidores",
    path: "/repartidores",
    icon: Navigation,
    roles: PERMISOS_RUTA["/repartidores"],
    element: RepartidoresPage,
  },
  {
    key: "transportistas",
    label: "Transportistas",
    path: "/transportistas",
    icon: Truck,
    roles: PERMISOS_RUTA["/transportistas"],
  },
  {
    key: "pagos",
    label: "Pagos y Cobranzas",
    path: "/pagos",
    icon: CreditCard,
    roles: PERMISOS_RUTA["/pagos"],
  },
  {
    key: "alertas",
    label: "Alertas",
    path: "/alertas",
    icon: Bell,
    roles: PERMISOS_RUTA["/alertas"],
    element: AlertasPage,
  },
  {
    key: "usuarios",
    label: "Usuarios",
    path: "/usuarios",
    icon: Users,
    roles: PERMISOS_RUTA["/usuarios"],
    element: UsuariosPage,
  },
  {
    key: "configuracion",
    label: "Configuración",
    path: "/configuracion",
    icon: Settings,
    roles: PERMISOS_RUTA["/configuracion"],
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
