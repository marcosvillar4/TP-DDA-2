import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Eye, Edit2 } from 'lucide-react';
import { useComercios } from '../../hooks/useComercios';
import BadgeComercio from './components/BadgeComercio';
import { ESTADO_COMERCIO_LABELS, esComercioActivo } from './utils/comercioEstado';
import './styles/ComerciosList.css';

const TODOS = 'TODOS';

export default function ComerciosList() {
  const navigate = useNavigate();
  const { comercios, loading, error } = useComercios();
  const [searchTerm, setSearchTerm] = useState('');
  const [estadoFilter, setEstadoFilter] = useState(TODOS);

  const term = searchTerm.toLowerCase();

  const filteredComercios = comercios.filter((c) => {
    const matchSearch =
      (c.nombreComercial ?? '').toLowerCase().includes(term) ||
      (c.razonSocial ?? '').toLowerCase().includes(term) ||
      (c.cuit ?? '').includes(searchTerm) ||
      (c.email ?? '').toLowerCase().includes(term);
    const matchEstado = estadoFilter === TODOS || c.estado === estadoFilter;
    return matchSearch && matchEstado;
  });

  const activosCount = filteredComercios.filter((c) => esComercioActivo(c.estado)).length;
  const noActivosCount = filteredComercios.length - activosCount;

  return (
    <div className="comercios-list-page">
      <div className="comercios-list-header">
        <div>
          <h1 className="comercios-list-title">Comercios</h1>
          <p className="comercios-list-subtitle">
            {filteredComercios.length} comercio{filteredComercios.length !== 1 ? 's' : ''} registrado{filteredComercios.length !== 1 ? 's' : ''}
          </p>
        </div>
      </div>

      <div className="comercios-filters-card">
        <div className="comercios-search-wrapper">
          <Search className="comercios-search-icon" size={18} />
          <input
            type="text"
            placeholder="Nombre, CUIT o email..."
            className="comercios-search-input"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <select
          className="comercios-select"
          value={estadoFilter}
          onChange={(e) => setEstadoFilter(e.target.value)}
        >
          <option value={TODOS}>Todos los estados</option>
          {Object.entries(ESTADO_COMERCIO_LABELS).map(([valor, label]) => (
            <option key={valor} value={valor}>{label}</option>
          ))}
        </select>
        {(searchTerm || estadoFilter !== TODOS) && (
          <button
            className="comercios-btn-limpiar"
            onClick={() => {
              setSearchTerm('');
              setEstadoFilter(TODOS);
            }}
          >
            Limpiar
          </button>
        )}
      </div>

      <div className="comercios-table-card">
        <div className="comercios-table-responsive">
          <table className="comercios-table">
            <thead>
              <tr>
                <th>COMERCIO</th>
                <th>CUIT</th>
                <th>EMAIL</th>
                <th>TELÉFONO</th>
                <th>DIRECCIÓN</th>
                <th>ESTADO</th>
                <th>ACCIONES</th>
              </tr>
            </thead>
            <tbody>
              {filteredComercios.map((c) => (
                <tr key={c.id}>
                  <td className="col-comercio-info">
                    <span className="comercio-nombre">{c.nombreComercial}</span>
                    <span className="comercio-responsable">{c.responsable}</span>
                  </td>
                  <td className="col-cuit">{c.cuit}</td>
                  <td className="col-email">{c.email}</td>
                  <td>{c.telefono}</td>
                  <td className="col-direccion">{c.direccion}</td>
                  <td>
                    <BadgeComercio estado={c.estado} />
                  </td>
                  <td className="col-acciones">
                    <button
                      className="comercios-btn-ver"
                      onClick={() => navigate(`/comercios/${c.id}`)}
                      title="Ver comercio"
                    >
                      <Eye size={16} /> Ver
                    </button>
                    <button
                      className="comercios-btn-icon"
                      title="Editar comercio"
                      onClick={() => navigate(`/comercios/${c.id}/editar`)}
                    >
                      <Edit2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
              {loading && (
                <tr>
                  <td colSpan="7" className="comercios-empty">Cargando comercios...</td>
                </tr>
              )}
              {!loading && error && (
                <tr>
                  <td colSpan="7" className="comercios-empty">{error}</td>
                </tr>
              )}
              {!loading && !error && filteredComercios.length === 0 && (
                <tr>
                  <td colSpan="7" className="comercios-empty">
                    No se encontraron comercios.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="comercios-pagination">
          <span className="pagination-info">Mostrando {filteredComercios.length} de {comercios.length} comercios</span>
          <div className="pagination-stats">
            <span className="badge-count activos">{activosCount} Activo{activosCount !== 1 ? 's' : ''}</span>
            <span className="badge-count inactivos">{noActivosCount} No activo{noActivosCount !== 1 ? 's' : ''}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
