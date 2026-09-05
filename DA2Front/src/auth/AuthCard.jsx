import "./styles/AuthCard.css";
 
export function AuthCard({ children }) {
  return (
    <div className="auth-card">
      <div className="auth-card-bar" />
      <div className="auth-card-body">{children}</div>
    </div>
  );
}