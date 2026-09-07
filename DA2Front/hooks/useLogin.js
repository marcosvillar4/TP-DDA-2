import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Swal from "sweetalert2";
import { loginRequest } from "../api/authApi";

export function useLogin() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();

    if (!email || !password) {
      Swal.fire({
        icon: "warning",
        title: "Faltan datos",
        text: "Completá correo y contraseña para continuar.",
        confirmButtonColor: "#16223f",
      });
      return;
    }

    setLoading(true);
    try {
      const data = await loginRequest({ email, password });

      localStorage.setItem("logired_token", data.token);
      localStorage.setItem(
        "logired_user",
        JSON.stringify({ id: data.id, username: data.username, rol: data.rol })
      );

      setUser(data);

      await Swal.fire({
        icon: "success",
        title: `¡Bienvenido, ${data.username}!`,
        timer: 1600,
        showConfirmButton: false,
      });

      // Redirigir al dashboard después del login exitoso
      navigate("/dashboard");

    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo iniciar sesión",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
    } finally {
      setLoading(false);
    }
  }

  return {
    email,
    setEmail,
    password,
    setPassword,
    loading,
    user,
    handleSubmit,
  };
}