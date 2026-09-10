import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import RegisterForm from "./auth/RegisterForm";
import DashboardLayout from "./layout/DashboardLayout";
import { EnTrabajoPage } from "./layout/components/EnTrabajoPage";
import { NAV_ITEMS } from "./layout/navConfig";

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
      </Route>
    </Routes>
  );
}

export default App;