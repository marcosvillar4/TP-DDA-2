export function formatMoneda(valor, moneda = "ARS") {
  const n = Number(valor);
  if (valor == null || Number.isNaN(n)) return "—";
  return new Intl.NumberFormat("es-AR", { style: "currency", currency: moneda }).format(n);
}
