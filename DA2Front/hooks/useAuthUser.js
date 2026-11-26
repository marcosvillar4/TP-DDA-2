import { useEffect, useState } from "react";

function leerUsuario() {
  try {
    const raw = localStorage.getItem("logired_user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function useAuthUser() {
  const [user, setUser] = useState(leerUsuario);

  useEffect(() => {
    function syncFromStorage() {
      setUser(leerUsuario());
    }

    window.addEventListener("storage", syncFromStorage);
    window.addEventListener("logired:usuario-actualizado", syncFromStorage);
    return () => {
      window.removeEventListener("storage", syncFromStorage);
      window.removeEventListener("logired:usuario-actualizado", syncFromStorage);
    };
  }, []);

  return user;
}