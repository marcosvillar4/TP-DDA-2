export function Button({ children, loading, ...props }) {
  return (
    <button
      className="flex w-full items-center justify-center rounded-lg bg-[#16223f] py-3 text-sm font-semibold text-white transition hover:bg-[#0f1830] disabled:opacity-60"
      disabled={loading}
      {...props}
    >
      {loading ? "Ingresando..." : children}
    </button>
  );
}