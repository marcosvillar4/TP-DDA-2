import "../styles/Button.css";

export function Button({ children, loading, ...props }) {
  return (
    <button className="button" disabled={loading} {...props}>
      {loading ? "Ingresando..." : children}
    </button>
  );
}