import { Link } from "react-router-dom";
import { useLogin } from "../../hooks/useLogin";
import { Button } from "./components/Button";
import { TextField } from "./components/TextField";
import { PasswordField } from "./components/PasswordField";
import { LinkText } from "./components/LinkText";
import { Logo } from "./components/Logo";
import "./styles/LoginForm.css";
 
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
      <div className="login-form-success">
        <Logo />
        <p className="login-form-success-text">
          Sesión iniciada como{" "}
          <span className="login-form-success-highlight">{user.username}</span>{" "}
          ({user.rol}).
        </p>
      </div>
    );
  }
 
  return (
    <form onSubmit={handleSubmit} className="login-form">
      <Logo />
 
      <div className="login-form-fields">
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
 
      <div className="login-form-link-row">
        <LinkText onClick={() => {}}>¿Olvidaste tu contraseña?</LinkText>
      </div>
 
      <Button type="submit" loading={loading}>
        Iniciar sesión
      </Button>
 
      <p className="login-form-footer-text">
        ¿No tenés cuenta?{" "}
        <Link to="/register" style={{ color: "var(--color-navy)", fontWeight: 600, textDecoration: "none" }}>
          Registrate aquí
        </Link>
      </p>
    </form>
  );
}
 