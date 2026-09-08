import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import RegisterForm from "./auth/RegisterForm";
import InventarioPage from "./inventario/InventarioPage";
import AppShell from "./shell/AppShell";
import DashboardPage from "./dashboard/DashboardPage";

function App() {
  return (
    <Routes>
      {/* Rutas públicas */}
      <Route path="/" element={<LoginPage />} />
      <Route path="/register" element={<RegisterForm />} />
      
      {/* Rutas con layout (AppShell) */}
      <Route element={<AppShell />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/pedidos" element={<div style={{padding: '2rem'}}>Página de Pedidos en construcción</div>} />
        <Route path="/seguimiento" element={<div style={{padding: '2rem'}}>Página de Seguimiento en construcción</div>} />
        <Route path="/comercios" element={<div style={{padding: '2rem'}}>Página de Comercios en construcción</div>} />
        <Route path="/inventario" element={<InventarioPage />} />
        <Route path="/depositos" element={<div style={{padding: '2rem'}}>Página de Depósitos en construcción</div>} />
        <Route path="/repartidores" element={<div style={{padding: '2rem'}}>Página de Repartidores en construcción</div>} />
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