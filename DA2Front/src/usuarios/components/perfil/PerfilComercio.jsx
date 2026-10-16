import { Building2, Landmark, FileText, MapPin } from "lucide-react";
import { InfoItem } from "../InfoItem";
import { formatearCuit } from "../../utils/usuarioLabels";

export function PerfilComercio({ perfil }) {
  return (
    <div className="detalle-datos">
      <InfoItem icono={Building2} label="Nombre comercial">{perfil.nombreComercial}</InfoItem>
      <InfoItem icono={Landmark} label="Razón social">{perfil.razonSocial}</InfoItem>
      <InfoItem icono={FileText} label="CUIT">{formatearCuit(perfil.cuit)}</InfoItem>
      <InfoItem icono={MapPin} label="Dirección de Comercio">{perfil.direccion}</InfoItem>
    </div>
  );
}