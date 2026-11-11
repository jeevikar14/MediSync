import { useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg, fmt } from '../api';
import { useAuth } from '../auth.jsx';
import { useFetch, usePatients } from '../hooks';
import { Alert, Badge, Btn, Card, Empty, Field, PageHeader, statusColor } from '../components/ui.jsx';

const blank = { patientId: '', doctorName: '', department: '', scheduledAt: '', reason: '', followUp: true };

export default function Appointments() {
  const { can } = useAuth();
  const [status, setStatus] = useState('');
  const [list, reload] = useFetch(`/appointments${status ? `?status=${status}` : ''}`);
  const { patients, name } = usePatients();
  const [doctors] = useFetch('/auth/users?role=DOCTOR');
  const [show, setShow] = useState(false);
  const [form, setForm] = useState(blank);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const writer = can('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE');

  const book = async (e) => {
    e.preventDefault(); setError('');
    try {
      await api.post('/appointments', { ...form, patientId: Number(form.patientId), scheduledAt: new Date(form.scheduledAt).toISOString() });
      setForm(blank); setShow(false); reload();
    } catch (err) { setError(errMsg(err)); }
  };
  const act = async (id, action) => {
    setError('');
    try { await api.post(`/appointments/${id}/${action}`, {}); reload(); } catch (err) { setError(errMsg(err)); }
  };

  return (
    <>
      <PageHeader title="Appointments" subtitle="Missing a follow-up raises a care gap automatically">
        <select className="inp w-40" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All</option><option>BOOKED</option><option>COMPLETED</option><option>MISSED</option>
        </select>
        {writer && <Btn onClick={() => setShow(!show)}>{show ? 'Cancel' : '+ Book appointment'}</Btn>}
      </PageHeader>
      <Alert>{error}</Alert>

      {show && (
        <Card title="Book appointment" className="mb-6">
          <form onSubmit={book} className="grid gap-4 md:grid-cols-3">
            <Field label="Patient">
              <select required className="inp" value={form.patientId} onChange={set('patientId')}>
                <option value="">Select...</option>{patients.map((p) => <option key={p.id} value={p.id}>{p.firstName} {p.lastName} ({p.mrn})</option>)}
              </select>
            </Field>
            <Field label="Doctor">
              <select required className="inp" value={form.doctorName} onChange={set('doctorName')}>
                <option value="">Select...</option>{doctors.map((d) => <option key={d.username} value={d.fullName}>{d.fullName}</option>)}
              </select>
            </Field>
            <Field label="Department"><input required className="inp" value={form.department} onChange={set('department')} /></Field>
            <Field label="Date & time"><input required type="datetime-local" className="inp" value={form.scheduledAt} onChange={set('scheduledAt')} /></Field>
            <Field label="Reason"><input className="inp" value={form.reason} onChange={set('reason')} /></Field>
            <label className="flex items-center gap-2 self-end pb-2 text-sm font-medium">
              <input type="checkbox" checked={form.followUp} onChange={(e) => setForm({ ...form, followUp: e.target.checked })} /> This is a follow-up
            </label>
            <div className="md:col-span-3"><Btn>Book</Btn></div>
          </form>
        </Card>
      )}

      <Card>
        {!list.length ? <Empty>No appointments.</Empty> : (
          <table className="w-full">
            <thead><tr><th className="th">When</th><th className="th">Patient</th><th className="th">Doctor</th><th className="th">Dept</th><th className="th">Type</th><th className="th">Status</th><th className="th" /></tr></thead>
            <tbody className="divide-y">
              {list.map((a) => (
                <tr key={a.id}>
                  <td className="td">{fmt(a.scheduledAt)}</td>
                  <td className="td"><Link className="font-medium text-teal-700" to={`/patients/${a.patientId}`}>{name(a.patientId)}</Link></td>
                  <td className="td">{a.doctorName}</td><td className="td">{a.department}</td>
                  <td className="td">{a.followUp ? <Badge color="purple">Follow-up</Badge> : 'Regular'}</td>
                  <td className="td"><Badge color={statusColor(a.status)}>{a.status}</Badge></td>
                  <td className="td">
                    {a.status === 'BOOKED' && writer && (
                      <div className="flex gap-2"><Btn variant="ghost" onClick={() => act(a.id, 'complete')}>Complete</Btn><Btn variant="danger" onClick={() => act(a.id, 'missed')}>Mark missed</Btn></div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </>
  );
}
