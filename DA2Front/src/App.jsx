import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import RegisterForm from "./auth/RegisterForm";
import InventarioPage from "./inventario/InventarioPage";
import AppShell from "./shell/AppShell";
import DashboardPage from "./dashboard/DashboardPage";
import PedidosList from "./pedidos/PedidosList";
import PedidoDetail from "./pedidos/PedidoDetail";
import SeguimientoPage from "./seguimiento/SeguimientoPage";
import ComerciosList from "./comercios/ComerciosList";
import ComercioForm from "./comercios/ComercioForm";
import ComercioDetail from "./comercios/ComercioDetail";
import RepartidoresPage from "./repartidores/RepartidoresPage";
import RepartidorDetail from "./repartidores/RepartidorDetail";

function App() {
  return (
    <Routes>
      {/* Rutas públicas */}
      <Route path="/" element={<LoginPage />} />
      <Route path="/register" element={<RegisterForm />} />
      
      {/* Rutas con layout (AppShell) */}
      <Route element={<AppShell />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/pedidos" element={<PedidosList />} />
        <Route path="/pedidos/:id" element={<PedidoDetail />} />
        <Route path="/seguimiento" element={<SeguimientoPage />} />
        <Route path="/seguimiento/:id" element={<SeguimientoPage />} />
        <Route path="/comercios" element={<ComerciosList />} />
        <Route path="/comercios/nuevo" element={<ComercioForm />} />
        <Route path="/comercios/:id" element={<ComercioDetail />} />
        <Route path="/comercios/:id/editar" element={<ComercioForm />} />
        <Route path="/inventario" element={<InventarioPage />} />
        <Route path="/depositos" element={<div style={{padding: '2rem'}}>Página de Depósitos en construcción</div>} />
        <Route path="/repartidores" element={<RepartidoresPage />} />
        <Route path="/repartidores/:id" element={<RepartidorDetail />} />
        <Route path="/pagos" element={<div style={{padding: '2rem'}}>Página de Pagos y Cobranzas en construcción</div>} />
        <Route path="/alertas" element={<div style={{padding: '2rem'}}>Página de Alertas en construcción</div>} />
        <Route path="/transportistas" element={<div style={{padding: '2rem'}}>Página de Transportistas en construcción</div>} />
        <Route path="/usuarios" element={<div style={{padding: '2rem'}}>Página de Usuarios en construcción</div>} />
        <Route path="/configuracion" element={<div style={{padding: '2rem'}}>Página de Configuración en construcción</div>} />
      </Route>
    </Routes>
  );
}

export default App;
