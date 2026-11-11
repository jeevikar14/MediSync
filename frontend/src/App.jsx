import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Patients from './pages/Patients.jsx';
import PatientDetails from './pages/PatientDetails.jsx';
import CareGaps from './pages/CareGaps.jsx';
import Handoffs from './pages/Handoffs.jsx';
import Appointments from './pages/Appointments.jsx';
import LabReports from './pages/LabReports.jsx';

function Protected({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<Protected><Layout /></Protected>}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/patients" element={<Patients />} />
        <Route path="/patients/:id" element={<PatientDetails />} />
        <Route path="/care-gaps" element={<CareGaps />} />
        <Route path="/handoffs" element={<Handoffs />} />
        <Route path="/appointments" element={<Appointments />} />
        <Route path="/lab-reports" element={<LabReports />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
