import { useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg, fmt } from '../api';
import { useAuth } from '../auth.jsx';
import { useFetch, usePatients } from '../hooks';
import { Alert, Badge, Btn, Card, Empty, Field, PageHeader, statusColor } from '../components/ui.jsx';

const blank = { patientId: '', toDoctor: '', fromDepartment: '', toDepartment: '', reason: '', clinicalSummary: '' };

export default function Handoffs() {
  const { user, can } = useAuth();
  const [filter, setFilter] = useState('');
  const [handoffs, reload] = useFetch(`/handoffs${filter === 'mine' ? `?toDoctor=${user.username}` : filter ? `?status=${filter}` : ''}`);
  const { patients, name } = usePatients();
  const [doctors] = useFetch('/auth/users?role=DOCTOR');
  const [show, setShow] = useState(false);
  const [form, setForm] = useState(blank);
  const [includeGaps, setIncludeGaps] = useState(true);
  const [extra, setExtra] = useState([]);
  const [extraText, setExtraText] = useState('');
  const [suggested] = useFetch(form.patientId ? `/handoffs/suggested-items?patientId=${form.patientId}` : null);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const create = async (e) => {
    e.preventDefault(); setError(''); setMsg('');
    try {
      const items = extra.map((d) => ({ itemType: 'OTHER', description: d }));
      const { data } = await api.post('/handoffs', { ...form, patientId: Number(form.patientId), includeOpenGaps: includeGaps, items });
      setMsg(`Handoff #${data.handoff.id} created with ${data.items.length} pending item(s). ${data.handoff.toDoctor} has been notified.`);
      setForm(blank); setExtra([]); setShow(false); reload();
    } catch (err) { setError(errMsg(err)); }
  };
  const ack = async (id) => {
    const comment = window.prompt('Acknowledgement comment (optional):', 'Received and reviewed pending items');
    if (comment === null) return;
    setError(''); setMsg('');
    try { await api.post(`/handoffs/${id}/acknowledge`, { comment }); setMsg(`Handoff #${id} acknowledged.`); reload(); }
    catch (err) { setError(errMsg(err)); }
  };

  return (
    <>
      <PageHeader title="Doctor Handoffs" subtitle="Pending items travel with the patient and the receiving doctor must acknowledge">
        <select className="inp w-44" value={filter} onChange={(e) => setFilter(e.target.value)}>
          <option value="">All</option><option value="mine">Addressed to me</option><option value="PENDING">Pending</option><option value="ACKNOWLEDGED">Acknowledged</option>
        </select>
        {can('ADMIN', 'DOCTOR') && <Btn onClick={() => setShow(!show)}>{show ? 'Cancel' : '+ New handoff'}</Btn>}
      </PageHeader>
      <Alert>{error}</Alert>
      <Alert type="ok">{msg}</Alert>

      {show && (
        <Card title="Create handoff" className="mb-6">
          <form onSubmit={create} className="grid gap-4 md:grid-cols-3">
            <Field label="Patient">
              <select required className="inp" value={form.patientId} onChange={set('patientId')}>
                <option value="">Select...</option>{patients.map((p) => <option key={p.id} value={p.id}>{p.firstName} {p.lastName} ({p.mrn})</option>)}
              </select>
            </Field>
            <Field label="Receiving doctor">
              <select required className="inp" value={form.toDoctor} onChange={set('toDoctor')}>
                <option value="">Select...</option>{doctors.filter((d) => d.username !== user.username).map((d) => <option key={d.username} value={d.username}>{d.fullName}</option>)}
              </select>
            </Field>
            <Field label="Reason"><input className="inp" value={form.reason} onChange={set('reason')} placeholder="Shift change, transfer..." /></Field>
            <Field label="From department"><input className="inp" value={form.fromDepartment} onChange={set('fromDepartment')} /></Field>
            <Field label="To department"><input className="inp" value={form.toDepartment} onChange={set('toDepartment')} /></Field>
            <div />
            <div className="md:col-span-3"><Field label="Clinical summary">
              <textarea required rows={3} className="inp" value={form.clinicalSummary} onChange={set('clinicalSummary')} />
            </Field></div>

            <div className="rounded-md bg-slate-50 p-3 md:col-span-3">
              <label className="flex items-center gap-2 text-sm font-medium">
                <input type="checkbox" checked={includeGaps} onChange={(e) => setIncludeGaps(e.target.checked)} />
                Automatically attach this patient's open care gaps as pending items
              </label>
              {form.patientId && (
                <ul className="mt-2 ml-6 list-disc text-sm text-slate-600">
                  {suggested.length ? suggested.map((g) => <li key={g.gapId}>[{g.severity}] {g.description}</li>) : <li className="list-none text-slate-400">No open care gaps for this patient.</li>}
                </ul>
              )}
              <div className="mt-3 flex gap-2">
                <input className="inp" placeholder="Add another pending item (e.g. call patient about results)" value={extraText} onChange={(e) => setExtraText(e.target.value)} />
                <Btn type="button" variant="ghost" onClick={() => { if (extraText.trim()) { setExtra([...extra, extraText.trim()]); setExtraText(''); } }}>Add</Btn>
              </div>
              <ul className="mt-2 ml-6 list-disc text-sm">{extra.map((x, i) => <li key={i}>{x}</li>)}</ul>
            </div>
            <div className="md:col-span-3"><Btn>Create handoff</Btn></div>
          </form>
        </Card>
      )}

      <div className="space-y-4">
        {!handoffs.length ? <Card><Empty>No handoffs.</Empty></Card> : handoffs.map(({ handoff: h, items }) => (
          <Card key={h.id}>
            <div className="flex items-start justify-between">
              <div>
                <div className="font-semibold">Handoff #{h.id} - <Link className="text-teal-700" to={`/patients/${h.patientId}`}>{name(h.patientId)}</Link> <Badge color={statusColor(h.status)}>{h.status}</Badge></div>
                <div className="text-sm text-slate-500">{h.fromDoctor} → {h.toDoctor}{h.toDepartment ? ` (${h.toDepartment})` : ''} - {fmt(h.createdAt)}{h.reason ? ` - ${h.reason}` : ''}</div>
              </div>
              {h.status === 'PENDING' && (h.toDoctor === user.username || user.role === 'ADMIN') && <Btn onClick={() => ack(h.id)}>Acknowledge</Btn>}
            </div>
            <p className="mt-2 text-sm">{h.clinicalSummary}</p>
            <div className="mt-2 text-xs font-semibold uppercase text-slate-500">Pending items ({items.length})</div>
            <ul className="ml-5 list-disc text-sm">{items.map((i) => <li key={i.id}><span className="text-xs text-slate-400">{i.itemType}</span> {i.description}</li>)}</ul>
            {h.status === 'ACKNOWLEDGED' && <div className="mt-2 text-xs text-emerald-700">Acknowledged by {h.acknowledgedBy} on {fmt(h.acknowledgedAt)}{h.ackComment ? ` - "${h.ackComment}"` : ''}</div>}
          </Card>
        ))}
      </div>
    </>
  );
}
