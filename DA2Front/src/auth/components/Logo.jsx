import logo from '../../assets/logo.png';
import "../styles/Logo.css";
 
export function Logo() {
  return (
    <div className="logo-wrapper">
      <img
        src={logo}
        alt="LogiRed - Logística de última milla"
        className="logo-image"
      />
    </div>
  );
}