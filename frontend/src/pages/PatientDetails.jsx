import { useState } from 'react';
import { useParams } from 'react-router-dom';
import api, { errMsg, fmt } from '../api';
import { useAuth } from '../auth.jsx';
import { useFetch } from '../hooks';
import { Alert, Badge, Btn, Card, Empty, PageHeader, severityColor, statusColor } from '../components/ui.jsx';

const ICON = {
  PATIENT_REGISTERED: ['bg-slate-400', 'Registered'], LAB_ORDERED: ['bg-sky-500', 'Lab ordered'],
  LAB_REPORT_UPLOADED: ['bg-amber-500', 'Lab report'], LAB_REPORT_REVIEWED: ['bg-emerald-500', 'Lab reviewed'],
  CLINICAL_NOTE_ADDED: ['bg-sky-500', 'Note'], APPOINTMENT_BOOKED: ['bg-sky-500', 'Appt booked'],
  APPOINTMENT_COMPLETED: ['bg-emerald-500', 'Appt completed'], APPOINTMENT_MISSED: ['bg-red-500', 'Appt missed'],
  CARE_GAP_DETECTED: ['bg-red-600', 'Care gap'], CARE_GAP_RESOLVED: ['bg-emerald-600', 'Gap resolved'],
  CARE_GAP_ESCALATED: ['bg-red-700', 'Escalated'], HANDOFF_CREATED: ['bg-violet-500', 'Handoff'],
  HANDOFF_ACKNOWLEDGED: ['bg-violet-700', 'Handoff ack'], EPISODE_STARTED: ['bg-teal-600', 'Pathway'], EPISODE_CLOSED: ['bg-teal-800', 'Episode closed'],
};

export default function PatientDetails() {
  const { id } = useParams();
  const { can } = useAuth();
  const [patient] = useFetch(`/patients/${id}`, null);
  const clinicalView = can('ADMIN', 'DOCTOR', 'NURSE');
  const [timeline, reloadTimeline] = useFetch(clinicalView ? `/care/timeline/${id}` : null);
  const [gaps, reloadGaps] = useFetch(clinicalView ? `/care/gaps?patientId=${id}` : null);
  const [episodes, reloadEpisodes] = useFetch(clinicalView ? `/care/episodes?patientId=${id}` : null);
  const [pathways] = useFetch(clinicalView ? '/care/pathways' : null);
  const [pathwayId, setPathwayId] = useState('');
  const [error, setError] = useState('');
  const [verification, setVerification] = useState({});
  const [newestFirst, setNewestFirst] = useState(true);

  const reloadAll = () => { reloadTimeline(); reloadGaps(); reloadEpisodes(); };

  const act = async (fn) => { setError(''); try { await fn(); reloadAll(); } catch (e) { setError(errMsg(e)); } };
  const start = () => act(() => api.post('/care/episodes', { patientId: Number(id), pathwayId: Number(pathwayId) }));
  const completeStep = (stepId) => act(() => api.post(`/care/episodes/steps/${stepId}/complete`));
  const verify = (epId) => act(async () => {
    const { data } = await api.get(`/care/episodes/${epId}/verify`);
    setVerification((v) => ({ ...v, [epId]: data }));
  });
  const close = (epId) => act(async () => {
    try { await api.post(`/care/episodes/${epId}/close`, { notes: 'Closed from UI after continuity verification' }); setVerification((v) => ({ ...v, [epId]: null })); }
    catch (e) { if (e.response?.status === 409 && e.response.data?.pendingMandatorySteps) setVerification((v) => ({ ...v, [epId]: e.response.data })); else throw e; }
  });

  if (!patient) return <Empty>Loading...</Empty>;
  const events = newestFirst ? timeline.slice().reverse() : timeline;

  return (
    <>
      <PageHeader title={`${patient.firstName} ${patient.lastName}`} subtitle={`${patient.mrn} - ${patient.gender} - DOB ${patient.dateOfBirth} - Blood ${patient.bloodGroup || '-'}`}>
        <Btn variant="ghost" onClick={reloadAll}>Refresh</Btn>
      </PageHeader>
      <Alert>{error}</Alert>
      {!clinicalView && <Card><p className="text-sm text-slate-500">Phone: {patient.phone || '-'} - Email: {patient.email || '-'}<br />Care timeline is available to clinical roles only.</p></Card>}

      {clinicalView && (
        <div className="grid gap-6 xl:grid-cols-2">
          <Card title="Patient Journey Timeline" actions={<Btn variant="ghost" onClick={() => setNewestFirst(!newestFirst)}>{newestFirst ? 'Newest first' : 'Oldest first'}</Btn>}>
            {!events.length ? <Empty>No events yet.</Empty> : (
              <ol className="relative max-h-[34rem] space-y-4 overflow-y-auto border-l-2 border-slate-200 pl-5">
                {events.map((t) => {
                  const [color, label] = ICON[t.eventType] || ['bg-slate-400', t.eventType];
                  return (
                    <li key={t.id} className="relative">
                      <span className={`absolute -left-[1.62rem] top-1 h-3 w-3 rounded-full ${color}`} />
                      <div className="text-xs text-slate-400">{fmt(t.occurredAt)} - {t.actor || 'system'}</div>
                      <div className="text-sm"><span className="mr-2 text-xs font-semibold uppercase text-slate-500">{label}</span>{t.summary}</div>
                    </li>
                  );
                })}
              </ol>
            )}
          </Card>

          <div className="space-y-6">
            <Card title="Care gaps">
              {!gaps.length ? <Empty>No care gaps recorded.</Empty> : (
                <ul className="divide-y">
                  {gaps.map((g) => (
                    <li key={g.id} className="py-2 text-sm">
                      <Badge color={statusColor(g.status)}>{g.status}</Badge>{' '}
                      <Badge color={severityColor(g.severity)}>{g.severity}</Badge>{' '}
                      <span className="text-xs text-slate-400">{g.gapType}</span>
                      <div>{g.description}</div>
                    </li>
                  ))}
                </ul>
              )}
            </Card>

            <Card title="Clinical pathways & continuity verification">
              {can('ADMIN', 'DOCTOR', 'NURSE') && (
                <div className="mb-4 flex gap-2">
                  <select className="inp" value={pathwayId} onChange={(e) => setPathwayId(e.target.value)}>
                    <option value="">Select a pathway to start...</option>
                    {pathways.map((p) => <option key={p.pathway.id} value={p.pathway.id}>{p.pathway.name}</option>)}
                  </select>
                  <Btn disabled={!pathwayId} onClick={start}>Start</Btn>
                </div>
              )}
              {!episodes.length ? <Empty>No care episodes.</Empty> : episodes.map(({ episode, steps }) => {
                const v = verification[episode.id];
                return (
                  <div key={episode.id} className="mb-4 rounded-md border p-3">
                    <div className="mb-2 flex items-center justify-between">
                      <div className="font-semibold">{episode.pathwayName} <Badge color={statusColor(episode.status)}>{episode.status}</Badge></div>
                      {episode.status === 'ACTIVE' && (
                        <div className="flex gap-2"><Btn variant="ghost" onClick={() => verify(episode.id)}>Verify</Btn><Btn onClick={() => close(episode.id)}>Close episode</Btn></div>
                      )}
                    </div>
                    <ul className="space-y-1 text-sm">
                      {steps.map((s) => (
                        <li key={s.id} className="flex items-center justify-between">
                          <span className={s.status === 'COMPLETED' ? 'text-slate-400 line-through' : ''}>
                            {s.status === 'COMPLETED' ? '✔' : '○'} {s.description} {!s.mandatory && <em className="text-xs text-slate-400">(optional)</em>}
                          </span>
                          {s.status === 'PENDING' && episode.status === 'ACTIVE' && <button className="text-xs text-teal-700" onClick={() => completeStep(s.id)}>mark done</button>}
                        </li>
                      ))}
                    </ul>
                    {v && (
                      <div className={`mt-3 rounded-md p-3 text-sm ${v.verified ? 'bg-emerald-50 text-emerald-800' : 'bg-red-50 text-red-800'}`}>
                        <div className="font-semibold">{v.verified ? 'Continuity verified - safe to close.' : 'Continuity check FAILED - cannot close:'}</div>
                        {[['Pending mandatory steps', v.pendingMandatorySteps], ['Open care gaps', v.openCareGaps], ['Unacknowledged handoffs', v.unacknowledgedHandoffs]]
                          .filter(([, l]) => l?.length).map(([title, l]) => (
                            <div key={title} className="mt-1"><div className="font-medium">{title}</div><ul className="ml-4 list-disc">{l.map((x, i) => <li key={i}>{x}</li>)}</ul></div>
                          ))}
                      </div>
                    )}
                  </div>
                );
              })}
            </Card>
          </div>
        </div>
      )}
    </>
  );
}
