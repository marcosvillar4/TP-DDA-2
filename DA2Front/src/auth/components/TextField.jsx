import "../styles/TextField.css";
 
export function TextField({ label, id, icon: Icon, ...props }) {
  return (
    <div className="text-field">
      <label htmlFor={id} className="text-field-label">
        {label}
      </label>
      {Icon ? (
        <div className="text-field-wrapper">
          <Icon className="text-field-icon" size={16} />
          <input id={id} className="text-field-input has-icon" {...props} />
        </div>
      ) : (
        <input id={id} className="text-field-input" {...props} />
      )}
    </div>
  );
}