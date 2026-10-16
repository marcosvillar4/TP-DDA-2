import { ChevronLeft, ChevronRight } from "lucide-react";

export function UsuariosPaginacion({ total, pagina, totalPaginas, porPagina, onCambiar }) {
  if (total === 0) return null;
  const desde = (pagina - 1) * porPagina + 1;
  const hasta = Math.min(pagina * porPagina, total);

  return (
    <div className="usuarios-paginacion">
      <span>
        Mostrando <strong>{desde}–{hasta}</strong> de <strong>{total}</strong>
      </span>
      {totalPaginas > 1 && (
        <div className="usuarios-paginacion-botones">
          <button
            type="button"
            className="u-btn u-btn-ghost u-btn-icon"
            onClick={() => onCambiar(pagina - 1)}
            disabled={pagina <= 1}
            aria-label="Página anterior"
          >
            <ChevronLeft size={16} />
          </button>
          <span>Página {pagina} de {totalPaginas}</span>
          <button
            type="button"
            className="u-btn u-btn-ghost u-btn-icon"
            onClick={() => onCambiar(pagina + 1)}
            disabled={pagina >= totalPaginas}
            aria-label="Página siguiente"
          >
            <ChevronRight size={16} />
          </button>
        </div>
      )}
    </div>
  );
}