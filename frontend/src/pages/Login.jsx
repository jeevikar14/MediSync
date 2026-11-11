import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { errMsg } from '../api';
import { Alert, Btn, Field } from '../components/ui.jsx';

const DEMO = [
  ['dr.smith', 'Doctor'], ['dr.jones', 'Doctor'], ['nurse.kim', 'Nurse'],
  ['lab.tech', 'Lab Technician'], ['reception', 'Receptionist'], ['admin', 'Admin'],
];

export default function Login() {
  const { user, login } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState('dr.smith');
  const [password, setPassword] = useState('Password@123');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true); setError('');
    try { await login(username, password); nav('/'); }
    catch (err) { setError(err.response?.status === 401 ? 'Invalid username or password' : errMsg(err)); }
    finally { setBusy(false); }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-900 p-4">
      <div className="w-full max-w-md rounded-xl bg-white p-8 shadow-xl">
        <h1 className="text-3xl font-bold text-teal-700">MediSync</h1>
        <p className="mb-6 text-sm text-slate-500">Intelligent Patient Care Continuity Platform</p>
        <Alert>{error}</Alert>
        <form onSubmit={submit} className="space-y-4">
          <Field label="Username"><input className="inp" value={username} onChange={(e) => setUsername(e.target.value)} /></Field>
          <Field label="Password"><input className="inp" type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></Field>
          <Btn className="w-full" disabled={busy}>{busy ? 'Signing in...' : 'Sign in'}</Btn>
        </form>
        <div className="mt-6 border-t pt-4">
          <p className="mb-2 text-xs font-semibold uppercase text-slate-400">Demo accounts (password Password@123)</p>
          <div className="flex flex-wrap gap-2">
            {DEMO.map(([u, r]) => (
              <button key={u} type="button" onClick={() => setUsername(u)} className="rounded bg-slate-100 px-2 py-1 text-xs hover:bg-slate-200">
                {u} <span className="text-slate-400">({r})</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
