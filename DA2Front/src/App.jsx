import { Routes, Route } from "react-router-dom";
import LoginPage from "./auth/LoginPage";
import RegisterForm from "./auth/RegisterForm";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/register" element={<RegisterForm />} />
    </Routes>
  );
}

export default App;
