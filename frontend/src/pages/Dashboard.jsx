import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { fmt } from '../api';
import { useAuth } from '../auth.jsx';
import { Badge, Btn, Card, Empty, PageHeader, severityColor } from '../components/ui.jsx';

export default function Dashboard() {
  const { user } = useAuth();
  const [d, setD] = useState({});

  const load = useCallback(async () => {
    const calls = {
      patients: '/patients', gaps: '/care/gaps?status=OPEN', handoffs: '/handoffs?status=PENDING',
      appointments: '/appointments?status=BOOKED', reports: '/clinical/lab-reports?reviewed=false', notifications: '/notifications',
    };
    const keys = Object.keys(calls);
    const res = await Promise.allSettled(keys.map((k) => api.get(calls[k])));
    const out = {};
    keys.forEach((k, i) => { out[k] = res[i].status === 'fulfilled' ? res[i].value.data : null; });
    setD(out);
  }, []);

  useEffect(() => { load(); const t = setInterval(load, 10000); return () => clearInterval(t); }, [load]);

  const markAll = async () => { await api.post('/notifications/read-all'); load(); };
  const stat = (label, value, to, color = 'text-slate-800') => value == null ? null : (
    <Link to={to} className="rounded-lg bg-white p-4 shadow-sm hover:shadow">
      <div className="text-xs font-semibold uppercase text-slate-500">{label}</div>
      <div className={`text-3xl font-bold ${color}`}>{value}</div>
    </Link>
  );
  const critical = d.gaps?.filter((g) => g.severity === 'CRITICAL').length;
  const mine = d.handoffs?.filter((h) => h.handoff.toDoctor === user.username).length;
  const unread = d.notifications?.filter((n) => !n.read) ?? [];

  return (
    <>
      <PageHeader title={`Welcome, ${user.fullName}`} subtitle="Live overview - refreshes every 10 seconds" />
      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-3 xl:grid-cols-6">
        {stat('Patients', d.patients?.length, '/patients')}
        {stat('Open care gaps', d.gaps?.length, '/care-gaps', 'text-red-600')}
        {stat('Critical gaps', critical, '/care-gaps', 'text-red-600')}
        {stat('Handoffs for me', mine, '/handoffs', 'text-amber-600')}
        {stat('Booked appts', d.appointments?.length, '/appointments')}
        {stat('Unreviewed labs', d.reports?.length, '/lab-reports', 'text-amber-600')}
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <Card title={`Notifications (${unread.length} unread)`} actions={unread.length > 0 && <Btn variant="ghost" onClick={markAll}>Mark all read</Btn>}>
          {!d.notifications?.length ? <Empty>No notifications yet.</Empty> : (
            <ul className="max-h-96 divide-y overflow-y-auto">
              {d.notifications.map((n) => (
                <li key={n.id} className={`py-2 text-sm ${n.read ? 'text-slate-400' : ''}`}>
                  <div className="flex justify-between"><Badge color={n.read ? 'gray' : 'blue'}>{n.type}</Badge><span className="text-xs">{fmt(n.createdAt)}</span></div>
                  <div className="mt-1">{n.message}</div>
                </li>
              ))}
            </ul>
          )}
        </Card>
        {d.gaps && (
          <Card title="Most urgent care gaps" actions={<Link className="text-sm text-teal-700" to="/care-gaps">View all</Link>}>
            {!d.gaps.length ? <Empty>No open care gaps.</Empty> : (
              <ul className="divide-y">
                {d.gaps.slice().sort((a, b) => (b.severity === 'CRITICAL') - (a.severity === 'CRITICAL')).slice(0, 6).map((g) => (
                  <li key={g.id} className="py-2 text-sm">
                    <Badge color={severityColor(g.severity)}>{g.severity}</Badge>{' '}
                    <Link className="text-teal-700" to={`/patients/${g.patientId}`}>Patient #{g.patientId}</Link>
                    <div>{g.description}</div>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        )}
      </div>
    </>
  );
}
