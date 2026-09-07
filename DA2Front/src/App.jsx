import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import RegisterForm from "./auth/RegisterForm";
import Dashboard from "./dashboard/Dashboard";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/register" element={<RegisterForm />} />
      <Route path="/dashboard" element={<Dashboard />} />
    </Routes>
  );
}

export default App;
