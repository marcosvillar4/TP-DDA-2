import "../styles/Select.css";

export function Select({ label, id, children, ...props }) {
  return (
    <div className="select-field">
      <label htmlFor={id} className="select-field-label">
        {label}
      </label>
      <select id={id} className="select-field-input" {...props}>
        {children}
      </select>
    </div>
  );
}