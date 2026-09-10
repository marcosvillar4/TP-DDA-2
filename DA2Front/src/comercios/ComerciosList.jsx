import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Plus, Eye, Edit2 } from 'lucide-react';
import { comerciosMock } from './mockData';
import BadgeComercio from './components/BadgeComercio';
import './styles/ComerciosList.css';

export default function ComerciosList() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('Todos los estados');

  const filteredComercios = comerciosMock.filter((c) => {
    const matchSearch = c.nombre.toLowerCase().includes(searchTerm.toLowerCase()) || 
                        c.cuit.includes(searchTerm) ||
                        c.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchEstado = estadoFilter === 'Todos los estados' || c.estado === estadoFilter;
    return matchSearch && matchEstado;
  });

  const activosCount = filteredComercios.filter(c => c.estado === 'Activo').length;
  const inactivosCount = filteredComercios.filter(c => c.estado === 'Inactivo').length;

  return (
    <div className="comercios-list-page">
      <div className="comercios-list-header">
        <div>
          <h1 className="comercios-list-title">Comercios</h1>
          <p className="comercios-list-subtitle">
            {filteredComercios.length} comercio{filteredComercios.length !== 1 ? 's' : ''} registrado{filteredComercios.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button className="comercios-btn-nuevo" onClick={() => navigate('/comercios/nuevo')}>
          <Plus size={16} />
          Nuevo comercio
        </button>
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
          <option value="Todos los estados">Todos los estados</option>
          <option value="Activo">Activo</option>
          <option value="Inactivo">Inactivo</option>
        </select>
        {(searchTerm || estadoFilter !== 'Todos los estados') && (
          <button 
            className="comercios-btn-limpiar"
            onClick={() => {
              setSearchTerm('');
              setEstadoFilter('Todos los estados');
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
              {filteredComercios.map(c => (
                <tr key={c.id}>
                  <td className="col-comercio-info">
                    <span className="comercio-nombre">{c.nombre}</span>
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
              {filteredComercios.length === 0 && (
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
          <span className="pagination-info">Mostrando {filteredComercios.length} de {comerciosMock.length} comercios</span>
          <div className="pagination-stats">
            <span className="badge-count activos">{activosCount} Activo{activosCount !== 1 ? 's' : ''}</span>
            <span className="badge-count inactivos">{inactivosCount} Inactivo{inactivosCount !== 1 ? 's' : ''}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
