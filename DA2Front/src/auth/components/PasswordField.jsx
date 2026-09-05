import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import "../styles/PasswordField.css";
 
export function PasswordField({ label, id, value, onChange, placeholder }) {
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
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          className="password-field-input"
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
 