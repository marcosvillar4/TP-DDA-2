import { useEffect, useState } from "react";

export function useAuthUser() {
  const [user, setUser] = useState(() => {
    try {
      const raw = localStorage.getItem("logired_user");
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    function syncFromStorage() {
      try {
        const raw = localStorage.getItem("logired_user");
        setUser(raw ? JSON.parse(raw) : null);
      } catch {
        setUser(null);
      }
    }

    window.addEventListener("storage", syncFromStorage);
    return () => window.removeEventListener("storage", syncFromStorage);
  }, []);

  return user;
}