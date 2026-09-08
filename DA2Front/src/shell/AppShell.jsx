import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import './AppShell.css';

export default function AppShell() {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-shell-right">
        <Header />
        <main className="app-shell-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
