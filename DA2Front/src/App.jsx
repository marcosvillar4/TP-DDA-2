import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import RegisterForm from "./auth/RegisterForm";
import DashboardLayout from "./layout/DashboardLayout";
import { EnTrabajoPage } from "./layout/components/EnTrabajoPage";
import { NAV_ITEMS } from "./layout/navConfig";
import ComercioDetail from "./comercios/ComercioDetail";
import ComercioForm from "./comercios/ComercioForm";
import PedidoDetail from "./pedidos/PedidoDetail";
import SeguimientoPage from "./seguimiento/SeguimientoPage";

/**
 * Rutas "hijas" que no son ítems de sidebar (detalle, alta, edición),
 * pero sí necesitan su propia ruta dentro del mismo layout fijo.
 * Se resuelven aparte de NAV_ITEMS porque no deben aparecer en el menú.
 * Mismo criterio que NAV_ITEMS: agregar acá, no tocar el resto.
 */
const EXTRA_ROUTES = [
  { path: "/comercios/nuevo", element: ComercioForm },
  { path: "/comercios/:id/editar", element: ComercioForm },
  { path: "/comercios/:id", element: ComercioDetail },
  { path: "/pedidos/:id", element: PedidoDetail },
  { path: "/seguimiento/:id", element: SeguimientoPage },
];

function App() {
  return (
    <Routes>
      {/* Rutas públicas */}
      <Route path="/" element={<LoginPage />} />
      <Route path="/register" element={<RegisterForm />} />

      <Route element={<DashboardLayout />}>
        {NAV_ITEMS.map((item) => (
          <Route
            key={item.key}
            path={item.path}
            element={
              item.element ? <item.element /> : <EnTrabajoPage titulo={item.label} />
            }
          />
        ))}

        {EXTRA_ROUTES.map((route) => (
          <Route key={route.path} path={route.path} element={<route.element />} />
        ))}
      </Route>
    </Routes>
  );
}

export default App;