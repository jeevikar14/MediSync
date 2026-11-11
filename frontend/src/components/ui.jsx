const COLORS = {
  gray: 'bg-slate-100 text-slate-700', red: 'bg-red-100 text-red-700', amber: 'bg-amber-100 text-amber-800',
  green: 'bg-emerald-100 text-emerald-700', blue: 'bg-sky-100 text-sky-700', purple: 'bg-violet-100 text-violet-700',
};

export const Badge = ({ color = 'gray', children }) => (
  <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-semibold ${COLORS[color]}`}>{children}</span>
);

export const severityColor = (s) => ({ CRITICAL: 'red', HIGH: 'amber', MEDIUM: 'blue' }[s] || 'gray');
export const statusColor = (s) =>
  ({ OPEN: 'red', RESOLVED: 'green', BOOKED: 'blue', COMPLETED: 'green', MISSED: 'red', PENDING: 'amber',
     ACKNOWLEDGED: 'green', ACTIVE: 'blue', CLOSED: 'gray', ORDERED: 'amber', RESULT_UPLOADED: 'blue', REVIEWED: 'green' }[s] || 'gray');

export const Card = ({ title, actions, children, className = '' }) => (
  <section className={`rounded-lg bg-white p-4 shadow-sm ${className}`}>
    {(title || actions) && (
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-base font-semibold">{title}</h2>
        <div className="flex gap-2">{actions}</div>
      </div>
    )}
    {children}
  </section>
);

export const Btn = ({ variant = 'primary', className = '', ...p }) => {
  const v = {
    primary: 'bg-teal-700 text-white hover:bg-teal-800',
    ghost: 'bg-white text-slate-700 border border-slate-300 hover:bg-slate-50',
    danger: 'bg-red-600 text-white hover:bg-red-700',
  }[variant];
  return <button {...p} className={`rounded-md px-3 py-1.5 text-sm font-medium disabled:opacity-50 ${v} ${className}`} />;
};

export const Alert = ({ type = 'error', children }) =>
  children ? (
    <div className={`mb-3 rounded-md px-3 py-2 text-sm ${type === 'error' ? 'bg-red-50 text-red-700' : 'bg-emerald-50 text-emerald-700'}`}>
      {children}
    </div>
  ) : null;

export const Field = ({ label, children }) => (
  <div>
    <label className="lbl">{label}</label>
    {children}
  </div>
);

export const Empty = ({ children = 'Nothing to show.' }) => <p className="py-6 text-center text-sm text-slate-400">{children}</p>;

export const PageHeader = ({ title, subtitle, children }) => (
  <div className="mb-5 flex items-end justify-between">
    <div>
      <h1 className="text-2xl font-bold">{title}</h1>
      {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
    </div>
    <div className="flex gap-2">{children}</div>
  </div>
);
