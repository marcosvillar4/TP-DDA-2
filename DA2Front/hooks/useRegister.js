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
 *     DEPOSITO   → nombreDeposito, direccionDeposito
 *     REPARTIDOR → vehiculo
 *
 * El JSON final es un único objeto plano con "rol" como discriminador,
 * tal como lo requiere @JsonTypeInfo del lado del backend.
 */
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
    setBaseData({ ...baseData, [e.target.name]: e.target.value });
  }

  function handleExtraChange(e) {
    setExtraData({ ...extraData, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();

    if (!userType) {
      Swal.fire({
        icon: "warning",
        title: "Falta el perfil",
        text: "Seleccioná un tipo de cuenta antes de continuar.",
        confirmButtonColor: "#16223f",
      });
      return;
    }

    if (baseData.password !== baseData.confirmPassword) {
      Swal.fire({
        icon: "warning",
        title: "Contraseñas distintas",
        text: "Las contraseñas no coinciden. Verificalas e intentá de nuevo.",
        confirmButtonColor: "#16223f",
      });
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