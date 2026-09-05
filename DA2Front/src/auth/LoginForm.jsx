import { useLogin } from "../../hooks/useLogin";
import { Button } from "./components/Button";
import { TextField } from "./components/TextField";
import { PasswordField } from "./components/PasswordField";
import { LinkText } from "./components/LinkText";
import { Logo } from "./components/Logo";

export function LoginForm() {
  const {
    email,
    setEmail,
    password,
    setPassword,
    loading,
    user,
    handleSubmit,
  } = useLogin();
 
  if (user) {
    return (
      <div className="flex flex-col items-center gap-4 text-center">
        <Logo />
        <p className="text-sm text-slate-600">
          Sesión iniciada como{" "}
          <span className="font-semibold text-[#16223f]">{user.username}</span>{" "}
          ({user.rol}).
        </p>
      </div>
    );
  }
 
  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-6">
      <Logo />
 
      <div className="flex flex-col gap-4">
        <TextField
          id="email"
          label="Correo electrónico"
          type="email"
          placeholder="admin@logired.com.ar"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoComplete="username"
        />
        <PasswordField
          id="password"
          label="Contraseña"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>
 
      <div className="flex justify-end">
        <LinkText onClick={() => {}}>¿Olvidaste tu contraseña?</LinkText>
      </div>
 
      <Button type="submit" loading={loading}>
        Iniciar sesión
      </Button>
 
      <p className="text-center text-sm text-slate-500">
        ¿Necesitás acceso? Contactá al administrador del sistema.
      </p>
    </form>
  );
}