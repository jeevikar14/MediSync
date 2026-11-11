import { useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg } from '../api';
import { useAuth } from '../auth.jsx';
import { useFetch } from '../hooks';
import { Alert, Badge, Btn, Card, Empty, Field, PageHeader } from '../components/ui.jsx';

const blank = { firstName: '', lastName: '', dateOfBirth: '', gender: 'MALE', phone: '', email: '', address: '', bloodGroup: '' };

export default function Patients() {
  const { can } = useAuth();
  const [q, setQ] = useState('');
  const [patients, reload] = useFetch(`/patients${q ? `?q=${encodeURIComponent(q)}` : ''}`);
  const [show, setShow] = useState(false);
  const [form, setForm] = useState(blank);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault(); setError('');
    try {
      const body = Object.fromEntries(Object.entries(form).map(([k, v]) => [k, v === '' ? null : v]));
      await api.post('/patients', body);
      setForm(blank); setShow(false); reload();
    } catch (err) { setError(errMsg(err)); }
  };

  return (
    <>
      <PageHeader title="Patients" subtitle="Synthetic data only">
        <input className="inp w-64" placeholder="Search name or MRN..." value={q} onChange={(e) => setQ(e.target.value)} />
        {can('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'NURSE') && <Btn onClick={() => setShow(!show)}>{show ? 'Cancel' : '+ Register patient'}</Btn>}
      </PageHeader>

      {show && (
        <Card title="Register patient" className="mb-6">
          <Alert>{error}</Alert>
          <form onSubmit={submit} className="grid gap-4 md:grid-cols-4">
            <Field label="First name"><input required className="inp" value={form.firstName} onChange={set('firstName')} /></Field>
            <Field label="Last name"><input required className="inp" value={form.lastName} onChange={set('lastName')} /></Field>
            <Field label="Date of birth"><input required type="date" className="inp" value={form.dateOfBirth} onChange={set('dateOfBirth')} /></Field>
            <Field label="Gender">
              <select className="inp" value={form.gender} onChange={set('gender')}><option>MALE</option><option>FEMALE</option><option>OTHER</option></select>
            </Field>
            <Field label="Phone"><input className="inp" value={form.phone} onChange={set('phone')} /></Field>
            <Field label="Email"><input type="email" className="inp" value={form.email} onChange={set('email')} /></Field>
            <Field label="Blood group"><input className="inp" maxLength={5} value={form.bloodGroup} onChange={set('bloodGroup')} /></Field>
            <Field label="Address"><input className="inp" value={form.address} onChange={set('address')} /></Field>
            <div className="md:col-span-4"><Btn>Save patient</Btn></div>
          </form>
        </Card>
      )}

      <Card>
        {!patients.length ? <Empty>No patients found.</Empty> : (
          <table className="w-full">
            <thead><tr><th className="th">MRN</th><th className="th">Name</th><th className="th">DOB</th><th className="th">Gender</th><th className="th">Phone</th><th className="th">Status</th></tr></thead>
            <tbody className="divide-y">
              {patients.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50">
                  <td className="td">{p.mrn}</td>
                  <td className="td"><Link className="font-medium text-teal-700" to={`/patients/${p.id}`}>{p.firstName} {p.lastName}</Link></td>
                  <td className="td">{p.dateOfBirth}</td><td className="td">{p.gender}</td><td className="td">{p.phone || '-'}</td>
                  <td className="td"><Badge color={p.status === 'ACTIVE' ? 'green' : 'gray'}>{p.status}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </>
  );
}
