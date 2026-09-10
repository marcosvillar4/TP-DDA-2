import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Plus, Eye } from 'lucide-react';
import { pedidosMock } from './mockData';
import BadgeEstado from './components/BadgeEstado';
import './styles/PedidosList.css';

export default function PedidosList() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('Todos los estados');
  const [comercioFilter, setComercioFilter] = useState('Todos los comercios');

  const filteredPedidos = pedidosMock.filter((p) => {
    const matchSearch = p.id.toLowerCase().includes(searchTerm.toLowerCase()) || 
                        p.comercio.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                        p.destinatario.toLowerCase().includes(searchTerm.toLowerCase());
    const matchEstado = estadoFilter === 'Todos los estados' || p.estado === estadoFilter;
    const matchComercio = comercioFilter === 'Todos los comercios' || p.comercio.nombre === comercioFilter;
    return matchSearch && matchEstado && matchComercio;
  });

  const uniqueEstados = ['Todos los estados', ...new Set(pedidosMock.map(p => p.estado))];
  const uniqueComercios = ['Todos los comercios', ...new Set(pedidosMock.map(p => p.comercio.nombre))];

  return (
    <div className="pedidos-list-page">
      <div className="pedidos-list-header">
        <div>
          <h1 className="pedidos-list-title">Pedidos</h1>
          <p className="pedidos-list-subtitle">
            {filteredPedidos.length} {filteredPedidos.length === 1 ? 'resultado' : 'resultados'}
          </p>
        </div>
        <button className="pedidos-btn-nuevo">
          <Plus size={16} />
          Nuevo pedido
        </button>
      </div>

      <div className="pedidos-filters-card">
        <div className="pedidos-search-wrapper">
          <Search className="pedidos-search-icon" size={18} />
          <input 
            type="text" 
            placeholder="ID de pedido, destinatario o comercio..."
            className="pedidos-search-input"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <select 
          className="pedidos-select"
          value={estadoFilter}
          onChange={(e) => setEstadoFilter(e.target.value)}
        >
          {uniqueEstados.map(e => <option key={e} value={e}>{e}</option>)}
        </select>
        <select 
          className="pedidos-select"
          value={comercioFilter}
          onChange={(e) => setComercioFilter(e.target.value)}
        >
          {uniqueComercios.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        {(searchTerm || estadoFilter !== 'Todos los estados' || comercioFilter !== 'Todos los comercios') && (
          <button 
            className="pedidos-btn-limpiar"
            onClick={() => {
              setSearchTerm('');
              setEstadoFilter('Todos los estados');
              setComercioFilter('Todos los comercios');
            }}
          >
            Limpiar filtros
          </button>
        )}
      </div>

      <div className="pedidos-table-card">
        <div className="pedidos-table-responsive">
          <table className="pedidos-table">
            <thead>
              <tr>
                <th>ID PEDIDO</th>
                <th>COMERCIO</th>
                <th>DESTINATARIO</th>
                <th>DIRECCIÓN</th>
                <th>ESTADO</th>
                <th>REPARTIDOR</th>
                <th>FECHA</th>
                <th>ACCIONES</th>
              </tr>
            </thead>
            <tbody>
              {filteredPedidos.map(p => (
                <tr key={p.id}>
                  <td className="col-id">{p.id}</td>
                  <td className="col-comercio">
                    <span className="comercio-nombre">{p.comercio.nombre}</span>
                    <span className="comercio-rubro">{p.comercio.rubro}</span>
                  </td>
                  <td>{p.destinatario}</td>
                  <td className="col-direccion">{p.direccion}</td>
                  <td>
                    <BadgeEstado estado={p.estado} />
                  </td>
                  <td>{p.repartidor}</td>
                  <td className="col-fecha">{p.fecha}</td>
                  <td>
                    <button 
                      className="pedidos-btn-ver"
                      onClick={() => navigate(`/pedidos/${p.id}`)}
                    >
                      <Eye size={16} /> Ver
                    </button>
                  </td>
                </tr>
              ))}
              {filteredPedidos.length === 0 && (
                <tr>
                  <td colSpan="8" className="pedidos-empty">
                    No se encontraron pedidos con los filtros aplicados.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        <div className="pedidos-pagination">
          <span className="pagination-info">Mostrando {filteredPedidos.length} de {pedidosMock.length} pedidos</span>
          <div className="pagination-controls">
            <button className="pagination-btn disabled">← Anterior</button>
            <span className="pagination-page">1</span>
            <button className="pagination-btn disabled">Siguiente →</button>
          </div>
        </div>
      </div>
    </div>
  );
}
