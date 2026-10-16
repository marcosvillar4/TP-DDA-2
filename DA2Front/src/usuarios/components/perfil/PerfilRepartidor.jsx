import { Truck, Hash, MapPin, Activity } from "lucide-react";
import { InfoItem } from "../InfoItem";
import {
  VEHICULO_LABELS,
  ESTADO_REPARTIDOR_LABELS,
  ESTADO_REPARTIDOR_CLASES,
} from "../../utils/usuarioLabels";

export function PerfilRepartidor({ perfil }) {
  return (
    <div className="detalle-datos">
      <InfoItem icono={Truck} label="Vehículo">
        {VEHICULO_LABELS[perfil.vehiculo] ?? perfil.vehiculo}
      </InfoItem>
      <InfoItem icono={Hash} label="Patente">
        {perfil.patente && <span className="patente">{perfil.patente}</span>}
      </InfoItem>
      <InfoItem icono={MapPin} label="Zona de reparto">{perfil.zona}</InfoItem>
      <InfoItem icono={Activity} label="Estado operativo">
        {perfil.estado && (
          <span className={`badge ${ESTADO_REPARTIDOR_CLASES[perfil.estado] ?? "badge-slate"}`}>
            <span className="badge-dot" aria-hidden="true" />
            {ESTADO_REPARTIDOR_LABELS[perfil.estado] ?? perfil.estado}
          </span>
        )}
      </InfoItem>
    </div>
  );
}