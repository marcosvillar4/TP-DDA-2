import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Swal from "sweetalert2";
import { registerRequest } from "../api/authApi";

/**
 * Hook de registro de usuario.
 *
 * Mapea los datos del formulario al RegisterDTO del backend:
 *   { username, email, password, rol }
 *
 * El campo "username" se construye a partir de los datos extra
 * según el rol elegido:
 *   - COMERCIO   → razón social
 *   - REPARTIDOR → nombre + apellido
 *   - DEPOSITO   → nombre del nodo
 */
export function useRegister() {
  const navigate = useNavigate();

  const [baseData, setBaseData] = useState({
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [userType, setUserType] = useState("");
  const [extraData, setExtraData] = useState({});
  const [loading, setLoading] = useState(false);

  function handleBaseChange(e) {
    setBaseData({ ...baseData, [e.target.name]: e.target.value });
  }

  function handleExtraChange(e) {
    setExtraData({ ...extraData, [e.target.name]: e.target.value });
  }

  /** Deriva el username display según el rol y los datos extra */
  function resolveUsername() {
    switch (userType) {
      case "COMERCIO":
        return extraData.razonSocial || baseData.email;
      case "REPARTIDOR":
        return `${extraData.nombre || ""} ${extraData.apellido || ""}`.trim() || baseData.email;
      case "DEPOSITO":
        return extraData.nombreNodo || baseData.email;
      default:
        return baseData.email;
    }
  }

  /** Valida la contraseña y devuelve un array con mensajes de error (vacío si pasa) */
  function validatePassword(password) {
    const errors = [];
    if (!password || password.length < 8) {
      errors.push("Al menos 8 caracteres.");
    }
    if (!/[A-Z]/.test(password)) {
      errors.push("Al menos una letra mayúscula.");
    }
    if (!/[a-z]/.test(password)) {
      errors.push("Al menos una letra minúscula.");
    }
    if (!/\d/.test(password)) {
      errors.push("Al menos un número.");
    }
    if (!/[\W_]/.test(password)) {
      errors.push("Al menos un carácter especial (por ejemplo: !@#$%^&*).");
    }
    return errors;
  }

  // Estado derivado: errores y flag de validez para feedback en vivo
  const passwordErrors = validatePassword(baseData.password);
  const passwordValid = passwordErrors.length === 0;

  async function handleSubmit(e) {
    e.preventDefault();

    // Validación: rol seleccionado
    if (!userType) {
      await Swal.fire({
        icon: "warning",
        title: "Falta el perfil",
        text: "Seleccioná un tipo de cuenta antes de continuar.",
        confirmButtonColor: "#16223f",
      });
      return;
    }

    // Validación: contraseñas coinciden
    if (baseData.password !== baseData.confirmPassword) {
      await Swal.fire({
        icon: "warning",
        title: "Contraseñas distintas",
        text: "Las contraseñas no coinciden. Verificalas e intentá de nuevo.",
        confirmButtonColor: "#16223f",
      });
      return;
    }

    // Validación: reglas de contraseña (cliente) -> mostrar con Swal si hay errores
    const pwdErrors = validatePassword(baseData.password);
    if (pwdErrors.length > 0) {
      const htmlList = `<ul style="text-align: left; margin: 0; padding-left: 1.25rem;">${pwdErrors
        .map((e) => `<li>${e}</li>`)
        .join("")}</ul>`;
      await Swal.fire({
        icon: "warning",
        title: "Contraseña inválida",
        html: `<p>La contraseña debe cumplir con los siguientes requisitos:</p>${htmlList}`,
        confirmButtonColor: "#16223f",
      });
      return;
    }

    setLoading(true);
    try {
      await registerRequest({
        username: resolveUsername(),
        email: baseData.email,
        password: baseData.password,
        rol: userType,
      });

      await Swal.fire({
        icon: "success",
        title: "¡Cuenta creada!",
        text: `Tu cuenta de ${userType.charAt(0) + userType.slice(1).toLowerCase()} fue registrada correctamente.`,
        confirmButtonColor: "#16223f",
        confirmButtonText: "Iniciar sesión",
      });

      navigate("/");
    } catch (err) {
      await Swal.fire({
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
    passwordErrors,
    passwordValid,
    setUserType,
    setExtraData,
    handleBaseChange,
    handleExtraChange,
    handleSubmit,
  };
}
