import { Link } from "react-router-dom";
import { Warehouse, MapPin, Building2, ArrowUpRight } from "lucide-react";
import { InfoItem } from "../InfoItem";

export function PerfilDeposito({ perfil }) {
  const comercio = perfil.comercio;

  return (
    <div className="detalle-datos">
      <InfoItem icono={Warehouse} label="Nombre del depósito">{perfil.nombre}</InfoItem>
      <InfoItem icono={MapPin} label="Dirección">{perfil.direccion}</InfoItem>
      <InfoItem icono={Building2} label="Comercio asociado" ancho>
        {comercio ? (
          comercio.usuarioId ? (
            <Link className="info-enlace" to={`/usuarios/${comercio.usuarioId}`}>
              {comercio.nombreComercial} <ArrowUpRight size={14} />
            </Link>
          ) : (
            comercio.nombreComercial
          )
        ) : (
          <span className="badge badge-amber">
            <span className="badge-dot" aria-hidden="true" />
            Sin comercio asociado
          </span>
        )}
      </InfoItem>
    </div>
  );
}