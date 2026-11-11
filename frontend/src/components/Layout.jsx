import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

const NAV = [
  { to: '/', label: 'Dashboard', roles: null },
  { to: '/patients', label: 'Patients', roles: null },
  { to: '/care-gaps', label: 'Care Gaps', roles: ['ADMIN', 'DOCTOR', 'NURSE'] },
  { to: '/handoffs', label: 'Handoffs', roles: ['ADMIN', 'DOCTOR', 'NURSE'] },
  { to: '/appointments', label: 'Appointments', roles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'] },
  { to: '/lab-reports', label: 'Lab Reports', roles: ['ADMIN', 'DOCTOR', 'NURSE', 'LAB_TECHNICIAN'] },
];

export default function Layout() {
  const { user, logout } = useAuth();
  return (
    <div className="flex min-h-screen">
      <aside className="w-56 shrink-0 bg-slate-900 p-4 text-slate-200">
        <div className="mb-6 text-xl font-bold text-teal-400">MediSync</div>
        <nav className="space-y-1">
          {NAV.filter((n) => !n.roles || n.roles.includes(user.role)).map((n) => (
            <NavLink key={n.to} to={n.to} end={n.to === '/'}
              className={({ isActive }) => `block rounded-md px-3 py-2 text-sm ${isActive ? 'bg-teal-700 text-white' : 'hover:bg-slate-800'}`}>
              {n.label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-8 border-t border-slate-700 pt-4 text-xs">
          <div className="font-semibold">{user.fullName}</div>
          <div className="text-slate-400">{user.role}</div>
          <button onClick={logout} className="mt-2 rounded bg-slate-700 px-2 py-1 hover:bg-slate-600">Log out</button>
        </div>
      </aside>
      <main className="flex-1 overflow-x-auto p-6"><Outlet /></main>
    </div>
  );
}
