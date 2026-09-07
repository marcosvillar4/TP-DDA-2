import "../styles/InventarioButton.css";

export function InventarioButton({
  children,
  variant = "primary",
  className = "",
  ...props
}) {
  return (
    <button
      type="button"
      className={`inventario-button inventario-button-${variant} ${className}`.trim()}
      {...props}
    >
      {children}
    </button>
  );
}