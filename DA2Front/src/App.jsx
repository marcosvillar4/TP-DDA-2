import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import InventarioPage from "./inventario/InventarioPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/inventario" element={<InventarioPage />} />
    </Routes>
  );
}

export default App;