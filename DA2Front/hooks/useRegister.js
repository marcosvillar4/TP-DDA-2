import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Swal from "sweetalert2";
import { registerRequest } from "../api/authApi";

/**
 * Hook de registro de usuario.
 *
 * Arma el RegisterDTO polimórfico que espera el backend:
 *   - baseData: campos comunes a cualquier rol
 *     (email, password, nombre, apellido, dni, telefono)
 *   - extraData: campos específicos del rol elegido
 *     COMERCIO   → nombreComercial, razonSocial, cuit, direccion
 *     REPARTIDOR → vehiculo, patente, zona
 *
 * DEPOSITO no es un rol de auto-registro: los depósitos los agrega un
 * comercio desde su panel.
 */

// Mismos formatos que el @Pattern del backend: AAA999 (viejo) o AA999AA (Mercosur).
const PATENTE_REGEX = /^([A-Z]{3}\d{3}|[A-Z]{2}\d{3}[A-Z]{2})$/;

export function useRegister() {
  const navigate = useNavigate();

  const [baseData, setBaseData] = useState({
    email: "",
    password: "",
    confirmPassword: "",
    nombre: "",
    apellido: "",
    dni: "",
    telefono: "",
  });

  const [userType, setUserType] = useState("");
  const [extraData, setExtraData] = useState({});
  const [loading, setLoading] = useState(false);

  function handleBaseChange(e) {
    const { name, value } = e.target;
    setBaseData((prev) => ({ ...prev, [name]: value }));
  }

  function handleExtraChange(e) {
    const { name, value } = e.target;
    // La patente se normaliza a mayúsculas y sin espacios mientras se escribe.
    const finalValue = name === "patente" ? value.toUpperCase().replace(/\s/g, "") : value;
    setExtraData((prev) => ({ ...prev, [name]: finalValue }));
  }

  function warn(title, text) {
    Swal.fire({ icon: "warning", title, text, confirmButtonColor: "#16223f" });
  }

  async function handleSubmit(e) {
    e.preventDefault();

    if (!userType) {
      warn("Falta el perfil", "Seleccioná un tipo de cuenta antes de continuar.");
      return;
    }

    if (baseData.password !== baseData.confirmPassword) {
      warn("Contraseñas distintas", "Las contraseñas no coinciden. Verificalas e intentá de nuevo.");
      return;
    }

    if (userType === "REPARTIDOR" && !PATENTE_REGEX.test(extraData.patente ?? "")) {
      warn("Patente inválida", "El formato debe ser AAA999 o AA999AA.");
      return;
    }

    setLoading(true);
    try {
      await registerRequest({
        email: baseData.email,
        password: baseData.password,
        nombre: baseData.nombre,
        apellido: baseData.apellido,
        dni: baseData.dni,
        telefono: baseData.telefono,
        rol: userType,
        ...extraData,
      });

      await Swal.fire({
        icon: "success",
        title: "¡Cuenta creada!",
        text: `Tu cuenta de ${userType.charAt(0) + userType.slice(1).toLowerCase()} fue registrada correctamente. Un administrador va a validarla antes de que puedas ingresar.`,
        confirmButtonColor: "#16223f",
        confirmButtonText: "Iniciar sesión",
      });

      navigate("/");
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "Error al registrarse",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
    } finally {
      setLoading(false);
    }
  }

  return {
    baseData,
    userType,
    extraData,
    loading,
    setUserType,
    setExtraData,
    handleBaseChange,
    handleExtraChange,
    handleSubmit,
  };
}