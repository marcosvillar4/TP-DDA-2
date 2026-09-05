import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";

export function PasswordField({ label, id, value, onChange, placeholder }) {
  const [visible, setVisible] = useState(false);

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-slate-700">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={visible ? "text" : "password"}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          className="w-full rounded-lg border border-slate-200 bg-slate-50 px-4 py-2.5 pr-11 text-sm text-slate-800 placeholder:text-slate-400 outline-none transition focus:border-[#16223f] focus:bg-white focus:ring-2 focus:ring-[#16223f]/10"
        />
        <button
          type="button"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
        >
          {visible ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
    </div>
  );
}