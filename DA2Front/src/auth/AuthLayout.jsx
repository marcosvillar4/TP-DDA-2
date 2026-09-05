import "./styles/AuthLayout.css";
 
export function AuthLayout({ children }) {
  return (
    <div className="auth-layout">
      <div className="auth-layout-circle-left" />
      <div className="auth-layout-circle-right" />
      <div className="auth-layout-content">{children}</div>
    </div>
  );
}