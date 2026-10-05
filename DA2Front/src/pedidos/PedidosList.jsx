import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Plus, Eye } from 'lucide-react';
import { getPedidos } from '../../api/pedidosApi';
import { getComercios } from '../../api/inventarioApi';
import BadgeEstado from './components/BadgeEstado';
import './styles/PedidosList.css';

export default function PedidosList() {
  const navigate = useNavigate();
  const [pedidos, setPedidos] = useState([]);
  const [comerciosMap, setComerciosMap] = useState({});
  const [loading, setLoading] = useState(true);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('Todos los estados');
  const [comercioFilter, setComercioFilter] = useState('Todos los comercios');

  useEffect(() => {
    Promise.all([getPedidos(), getComercios()])
      .then(([pedidosData, comerciosData]) => {
        setPedidos(pedidosData);
        
        const map = {};
        comerciosData.forEach(c => {
          map[c.id] = c;
        });
        setComerciosMap(map);
      })
      .catch(err => console.error("Error cargando pedidos:", err))
      .finally(() => setLoading(false));
  }, []);

  const filteredPedidos = pedidos.filter((p) => {
    const comercio = comerciosMap[p.comercioId] || { nombre: `Comercio ${p.comercioId}` };
    const idStr = p.id.toString();
    const matchSearch = idStr.includes(searchTerm) || 
                        comercio.nombreComercial.toLowerCase().includes(searchTerm.toLowerCase());
    const matchEstado = estadoFilter === 'Todos los estados' || p.estado === estadoFilter;
    const matchComercio = comercioFilter === 'Todos los comercios' || comercio.nombreComercial === comercioFilter;
    return matchSearch && matchEstado && matchComercio;
  });

  const uniqueEstados = ['Todos los estados', ...new Set(pedidos.map(p => p.estado))];
  const uniqueComercios = ['Todos los comercios', ...new Set(pedidos.map(p => comerciosMap[p.comercioId]?.nombre || `Comercio ${p.comercioId}`))];

  return (
    <div className="pedidos-list-page">
      <div className="pedidos-list-header">
        <div>
          <h1 className="pedidos-list-title">Pedidos</h1>
          <p className="pedidos-list-subtitle">
            {filteredPedidos.length} {filteredPedidos.length === 1 ? 'resultado' : 'resultados'}
          </p>
        </div>
        <button className="pedidos-btn-nuevo" onClick={() => navigate('/pedidos/nuevo')}>
          <Plus size={16} />
          Nuevo pedido
        </button>
      </div>

      <div className="pedidos-filters-card">
        <div className="pedidos-search-wrapper">
          <Search className="pedidos-search-icon" size={18} />
          <input 
            type="text" 
            placeholder="ID de pedido o comercio..."
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
                <th>DIRECCIÓN DESTINO</th>
                <th>ESTADO</th>
                <th>REPARTIDOR</th>
                <th>ÚLTIMA ACTUALIZACIÓN</th>
                <th>ACCIONES</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="7" style={{textAlign: 'center', padding: '2rem'}}>Cargando pedidos...</td></tr>
              ) : filteredPedidos.map(p => {
                const comercio = comerciosMap[p.comercioId] || { nombre: `Comercio ${p.comercioId}`, rubro: '-' };
                const lastHistorial = p.historial && p.historial.length > 0 ? p.historial[0] : null;
                const fechaStr = lastHistorial ? new Date(lastHistorial.fechaHora).toLocaleString() : '-';

                return (
                  <tr key={p.id}>
                    <td className="col-id">LOG-{p.id}</td>
                    <td className="col-comercio">
                      <span className="comercio-nombre">{comercio.nombreComercial}</span>
                      <span className="comercio-rubro">{comercio.rubro}</span>
                    </td>
                    <td className="col-direccion">{p.direccionDestino}</td>
                    <td>
                      <BadgeEstado estado={p.estado} />
                    </td>
                    <td>{p.repartidorNombre || 'Sin asignar'}</td>
                    <td className="col-fecha">{fechaStr}</td>
                    <td>
                      <button 
                        className="pedidos-btn-ver"
                        onClick={() => navigate(`/pedidos/${p.id}`)}
                      >
                        <Eye size={16} /> Ver
                      </button>
                    </td>
                  </tr>
                );
              })}
              {!loading && filteredPedidos.length === 0 && (
                <tr>
                  <td colSpan="7" className="pedidos-empty">
                    No se encontraron pedidos.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        <div className="pedidos-pagination">
          <span className="pagination-info">Mostrando {filteredPedidos.length} de {pedidos.length} pedidos</span>
        </div>
      </div>
    </div>
  );
}

