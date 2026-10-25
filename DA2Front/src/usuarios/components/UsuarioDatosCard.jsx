import { Mail, Phone, Contact } from "lucide-react";
import { InfoItem } from "./InfoItem";

export function UsuarioDatosCard({ usuario }) {
  return (
    <div className="detalle-datos">
      <InfoItem icono={Mail} label="Email">{usuario.email}</InfoItem>
      <InfoItem icono={Phone} label="Teléfono">{usuario.telefono}</InfoItem>
      <InfoItem icono={Contact} label="DNI">{usuario.dni}</InfoItem>
    </div>
  );
}