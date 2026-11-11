import { useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg, fmt } from '../api';
import { useAuth } from '../auth.jsx';
import { useFetch, usePatients } from '../hooks';
import { Alert, Badge, Btn, Card, Empty, Field, PageHeader, statusColor } from '../components/ui.jsx';

export default function LabReports() {
  const { can } = useAuth();
  const [onlyUnreviewed, setOnlyUnreviewed] = useState(false);
  const [orders, reloadOrders] = useFetch('/clinical/lab-orders');
  const [reports, reloadReports] = useFetch(`/clinical/lab-reports${onlyUnreviewed ? '?reviewed=false' : ''}`);
  const { patients, name } = usePatients();
  const [order, setOrder] = useState({ patientId: '', testName: '', priority: 'ROUTINE' });
  const [upload, setUpload] = useState({ labOrderId: '', resultSummary: '', abnormal: false });
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const reload = () => { reloadOrders(); reloadReports(); };

  const run = async (fn, ok) => {
    setError(''); setMsg('');
    try { await fn(); setMsg(ok); reload(); } catch (e) { setError(errMsg(e)); }
  };
  const placeOrder = (e) => { e.preventDefault(); run(async () => { await api.post('/clinical/lab-orders', { ...order, patientId: Number(order.patientId) }); setOrder({ patientId: '', testName: '', priority: 'ROUTINE' }); }, 'Lab order placed.'); };
  const uploadReport = (e) => { e.preventDefault(); run(async () => { await api.post('/clinical/lab-reports', { ...upload, labOrderId: Number(upload.labOrderId) }); setUpload({ labOrderId: '', resultSummary: '', abnormal: false }); }, 'Report uploaded - event LAB_REPORT_UPLOADED published to Kafka.'); };
  const review = (id) => {
    const comment = window.prompt('Review comment (optional):', '');
    if (comment === null) return;
    run(() => api.post(`/clinical/lab-reports/${id}/review`, { comment }), 'Report reviewed.');
  };
  const pendingOrders = orders.filter((o) => o.status === 'ORDERED');

  return (
    <>
      <PageHeader title="Lab Orders & Reports" subtitle="Doctors order, laboratory uploads, doctors review">
        <Btn variant="ghost" onClick={reload}>Refresh</Btn>
      </PageHeader>
      <Alert>{error}</Alert>
      <Alert type="ok">{msg}</Alert>

      <div className="mb-6 grid gap-6 lg:grid-cols-2">
        {can('ADMIN', 'DOCTOR') && (
          <Card title="Order a lab test">
            <form onSubmit={placeOrder} className="grid gap-3 md:grid-cols-2">
              <Field label="Patient">
                <select required className="inp" value={order.patientId} onChange={(e) => setOrder({ ...order, patientId: e.target.value })}>
                  <option value="">Select...</option>{patients.map((p) => <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>)}
                </select>
              </Field>
              <Field label="Test name"><input required className="inp" value={order.testName} onChange={(e) => setOrder({ ...order, testName: e.target.value })} placeholder="HbA1c, CBC, Lipid profile..." /></Field>
              <Field label="Priority">
                <select className="inp" value={order.priority} onChange={(e) => setOrder({ ...order, priority: e.target.value })}><option>ROUTINE</option><option>URGENT</option></select>
              </Field>
              <div className="self-end"><Btn>Place order</Btn></div>
            </form>
          </Card>
        )}
        {can('ADMIN', 'LAB_TECHNICIAN') && (
          <Card title="Upload a lab report">
            <form onSubmit={uploadReport} className="space-y-3">
              <Field label="Order awaiting result">
                <select required className="inp" value={upload.labOrderId} onChange={(e) => setUpload({ ...upload, labOrderId: e.target.value })}>
                  <option value="">Select...</option>
                  {pendingOrders.map((o) => <option key={o.id} value={o.id}>#{o.id} - {o.testName} - {name(o.patientId)}</option>)}
                </select>
              </Field>
              <Field label="Result summary"><textarea required rows={2} className="inp" value={upload.resultSummary} onChange={(e) => setUpload({ ...upload, resultSummary: e.target.value })} /></Field>
              <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={upload.abnormal} onChange={(e) => setUpload({ ...upload, abnormal: e.target.checked })} /> Abnormal result</label>
              <Btn>Upload report</Btn>
            </form>
          </Card>
        )}
      </div>

      <Card title="Lab reports" className="mb-6" actions={
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={onlyUnreviewed} onChange={(e) => setOnlyUnreviewed(e.target.checked)} /> Unreviewed only</label>}>
        {!reports.length ? <Empty>No lab reports.</Empty> : (
          <table className="w-full">
            <thead><tr><th className="th">Patient</th><th className="th">Test</th><th className="th">Result</th><th className="th">Uploaded</th><th className="th">Review</th><th className="th" /></tr></thead>
            <tbody className="divide-y">
              {reports.map((r) => (
                <tr key={r.id}>
                  <td className="td"><Link className="font-medium text-teal-700" to={`/patients/${r.patientId}`}>{name(r.patientId)}</Link></td>
                  <td className="td">{r.testName} {r.abnormal && <Badge color="red">ABNORMAL</Badge>}</td>
                  <td className="td">{r.resultSummary}</td>
                  <td className="td text-xs">{fmt(r.uploadedAt)}<br />by {r.uploadedBy}</td>
                  <td className="td">{r.reviewed ? <><Badge color="green">REVIEWED</Badge><div className="text-xs">{r.reviewedBy} - {fmt(r.reviewedAt)}</div></> : <Badge color="amber">NOT REVIEWED</Badge>}</td>
                  <td className="td">{!r.reviewed && can('ADMIN', 'DOCTOR') && <Btn onClick={() => review(r.id)}>Mark reviewed</Btn>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <Card title="Lab orders">
        {!orders.length ? <Empty>No lab orders.</Empty> : (
          <table className="w-full">
            <thead><tr><th className="th">#</th><th className="th">Patient</th><th className="th">Test</th><th className="th">Priority</th><th className="th">Ordered by</th><th className="th">Status</th></tr></thead>
            <tbody className="divide-y">
              {orders.map((o) => (
                <tr key={o.id}><td className="td">{o.id}</td><td className="td">{name(o.patientId)}</td><td className="td">{o.testName}</td>
                  <td className="td">{o.priority}</td><td className="td">{o.orderedBy}</td><td className="td"><Badge color={statusColor(o.status)}>{o.status}</Badge></td></tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </>
  );
}
