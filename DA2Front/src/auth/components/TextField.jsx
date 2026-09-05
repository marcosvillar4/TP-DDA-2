import "../styles/TextField.css";
 
export function TextField({ label, id, ...props }) {
  return (
    <div className="text-field">
      <label htmlFor={id} className="text-field-label">
        {label}
      </label>
      <input id={id} className="text-field-input" {...props} />
    </div>
  );
}