import { useState } from 'react';
import { Link } from 'react-router-dom';
import { fmt } from '../api';
import { useFetch, usePatients } from '../hooks';
import { Badge, Btn, Card, Empty, PageHeader, severityColor, statusColor } from '../components/ui.jsx';

const TYPE_LABEL = { LAB_REPORT_UNREVIEWED: 'Unreviewed lab report', MISSED_FOLLOWUP: 'Missed follow-up', COMBINED: 'Combined (lab + missed)' };

export default function CareGaps() {
  const [status, setStatus] = useState('OPEN');
  const [gaps, reload] = useFetch(`/care/gaps${status ? `?status=${status}` : ''}`);
  const { name } = usePatients();

  return (
    <>
      <PageHeader title="Care Gaps" subtitle="Detected automatically from Kafka events; unresolved gaps escalate to CRITICAL after the configured delay">
        <select className="inp w-40" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="OPEN">Open</option><option value="RESOLVED">Resolved</option><option value="">All</option>
        </select>
        <Btn variant="ghost" onClick={reload}>Refresh</Btn>
      </PageHeader>
      <Card>
        {!gaps.length ? <Empty>No care gaps in this view.</Empty> : (
          <table className="w-full">
            <thead><tr><th className="th">Patient</th><th className="th">Type</th><th className="th">Description</th><th className="th">Severity</th><th className="th">Status</th><th className="th">Detected</th><th className="th">Resolved</th></tr></thead>
            <tbody className="divide-y">
              {gaps.map((g) => (
                <tr key={g.id}>
                  <td className="td"><Link className="font-medium text-teal-700" to={`/patients/${g.patientId}`}>{name(g.patientId)}</Link></td>
                  <td className="td">{TYPE_LABEL[g.gapType] || g.gapType}</td>
                  <td className="td">{g.description}</td>
                  <td className="td"><Badge color={severityColor(g.severity)}>{g.severity}</Badge></td>
                  <td className="td"><Badge color={statusColor(g.status)}>{g.status}</Badge></td>
                  <td className="td text-xs">{fmt(g.detectedAt)}</td>
                  <td className="td text-xs">{g.resolvedAt ? `${fmt(g.resolvedAt)} (${g.resolutionNote || ''})` : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </>
  );
}
