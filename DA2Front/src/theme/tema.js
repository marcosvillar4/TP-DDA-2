const CLAVE_USUARIO = "logired_user";

export const TEMAS = { CLARO: "CLARO", OSCURO: "OSCURO" };

/** Aplica el tema al <html>. Cualquier valor distinto de OSCURO es claro. */
export function aplicarTema(tema) {
  document.documentElement.setAttribute(
    "data-tema",
    tema === TEMAS.OSCURO ? "oscuro" : "claro"
  );
}

/** Tema del usuario logueado (guardado en localStorage). Claro si no hay sesión. */
export function leerTemaGuardado() {
  try {
    const raw = localStorage.getItem(CLAVE_USUARIO);
    return (raw && JSON.parse(raw).tema) || TEMAS.CLARO;
  } catch {
    return TEMAS.CLARO;
  }
}

/** Actualiza el tema en la sesión local y avisa a los componentes que escuchan. */
export function guardarTemaEnSesion(tema) {
  try {
    const raw = localStorage.getItem(CLAVE_USUARIO);
    if (!raw) return;
    localStorage.setItem(CLAVE_USUARIO, JSON.stringify({ ...JSON.parse(raw), tema }));
    window.dispatchEvent(new Event("logired:usuario-actualizado"));
  } catch {
    // Si localStorage falla, el tema igual queda aplicado en esta pantalla.
  }
}