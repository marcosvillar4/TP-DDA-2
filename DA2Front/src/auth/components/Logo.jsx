import logo from '../../assets/logo.png';

export function Logo() {
  return (
    <div className="flex justify-center">
      <img src={logo} alt="LogiRed - Logística de última milla" className="h-40 w-auto" />
    </div>
  );
}