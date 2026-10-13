import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import "../styles/PasswordField.css";
 
export function PasswordField({ label, id, ...props }) {
  const [visible, setVisible] = useState(false);
 
  return (
    <div className="password-field">
      <label htmlFor={id} className="password-field-label">
        {label}
      </label>
      <div className="password-field-wrapper">
        <input
          id={id}
          type={visible ? "text" : "password"}
          className="password-field-input"
          {...props}
        />
        <button
          type="button"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
          className="password-field-toggle"
        >
          {visible ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
    </div>
  );
}