import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation, useNavigate } from 'react-router';
import { Menu } from 'lucide-react';
import { Sidebar } from './components/Sidebar';
import { Header } from './components/Header';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LoginScreen } from './screens/LoginScreen';
import { DashboardScreen } from './screens/DashboardScreen';
import { DoctorDashboard } from './screens/DoctorDashboard';
import { ReceptionistDashboard } from './screens/ReceptionistDashboard';
import { PacientesScreen } from './screens/PacientesScreen';
import { UsuariosScreen } from './screens/UsuariosScreen';
import { RecepcionistasScreen } from './screens/RecepcionistasScreen';
import { AgendamientoScreen } from './screens/AgendamientoScreen';
import { HistorialScreen } from './screens/HistorialScreen';
import { ConsultaScreen } from './screens/ConsultaScreen';
import { ReportesScreen } from './screens/ReportesScreen';
import { NotificacionesScreen } from './screens/NotificacionesScreen';
import { RegistroPacienteScreen } from './screens/RegistroPacienteScreen';
import { AgendaGlobalScreen } from './screens/AgendaGlobalScreen';
import { MiAgendaScreen } from './screens/MiAgendaScreen';
import { MisPacientesScreen } from './screens/MisPacientesScreen';
import { AdministracionPlantillasScreen } from './screens/AdministracionPlantillasScreen';
import { useEffect, useState, type ReactNode } from 'react';
import type { UserRole } from './types/user';

function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    setIsOpen(false);
  }, [location.pathname]);

  if (!user) return null;

  return (
    <div className="flex min-h-screen flex-col bg-[#F4F7F9] lg:flex-row">
      <button
        type="button"
        aria-label="Abrir menú"
        onClick={() => setIsOpen(true)}
        className="fixed left-4 top-4 z-50 inline-flex h-10 w-10 items-center justify-center rounded-lg border border-[#CBD5E1] bg-white text-[#1E293B] shadow-sm lg:hidden"
      >
        <Menu className="h-5 w-5" />
      </button>

      <div
        className={`fixed inset-0 z-40 bg-slate-900/30 transition-opacity lg:hidden ${isOpen ? 'opacity-100' : 'pointer-events-none opacity-0'}`}
        onClick={() => setIsOpen(false)}
      />

      <Sidebar
        activeScreen={location.pathname.slice(1) || 'dashboard'}
        onNavigate={(screen) => {
          navigate(`/${screen}`);
          setIsOpen(false);
        }}
        userRole={user.role}
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
      />

      <div className="flex min-h-0 flex-1 flex-col overflow-hidden md:ml-0">
        <Header user={user} onLogout={logout} />
        <main className="flex-1 overflow-auto"><Outlet /></main>
      </div>
    </div>
  );
}

function RoleRoute({ roles, children }: { roles: UserRole[]; children: ReactNode }) {
  const { user } = useAuth();
  return user && roles.includes(user.role) ? <>{children}</> : <Navigate to="/dashboard" replace />;
}

function DashboardRoute() {
  const { user } = useAuth();

  if (user?.role === 'DOCTOR') {
    return <DoctorDashboard />;
  }

  if (user?.role === 'RECEPTIONIST') {
    return <ReceptionistDashboard />;
  }

  return <DashboardScreen />;
}

function PatientsRoute() {
  const { user } = useAuth();
  const navigate = useNavigate();

  if (!user) return <Navigate to="/login" replace />;
  if (user.role === 'RECEPTIONIST') return <PacientesScreen />;
  if (user.role === 'DOCTOR') {
    return <MisPacientesScreen onVerHistorial={(medicalRecordId) => navigate(`/historial?medicalRecordId=${medicalRecordId}`)} />;
  }
  return <Navigate to="/dashboard" replace />;
}

function DoctorAgendaRoute() {
  const navigate = useNavigate();
  return <MiAgendaScreen onIniciarConsulta={(consultationId) => navigate(`/consulta?consultationId=${consultationId}`)} />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginScreen />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<DashboardRoute />} />
          <Route path="/pacientes" element={<PatientsRoute />} />
          <Route path="/usuarios" element={<RoleRoute roles={['ADMIN']}><UsuariosScreen /></RoleRoute>} />
          <Route path="/recepcionistas" element={<RoleRoute roles={['ADMIN']}><RecepcionistasScreen /></RoleRoute>} />
          <Route path="/plantillas" element={<RoleRoute roles={['ADMIN']}><AdministracionPlantillasScreen /></RoleRoute>} />
          <Route path="/citas-global" element={<RoleRoute roles={['ADMIN', 'RECEPTIONIST']}><AgendaGlobalScreen /></RoleRoute>} />
          <Route path="/reportes" element={<RoleRoute roles={['ADMIN']}><ReportesScreen /></RoleRoute>} />
          <Route path="/agendamiento" element={<RoleRoute roles={['RECEPTIONIST']}><AgendamientoScreen /></RoleRoute>} />
          <Route path="/notificaciones" element={<RoleRoute roles={['RECEPTIONIST']}><NotificacionesScreen /></RoleRoute>} />
          <Route path="/mi-agenda" element={<RoleRoute roles={['DOCTOR']}><DoctorAgendaRoute /></RoleRoute>} />
          <Route path="/historial" element={<RoleRoute roles={['DOCTOR']}><HistorialScreen /></RoleRoute>} />
          <Route path="/consulta" element={<RoleRoute roles={['DOCTOR']}><ConsultaScreen /></RoleRoute>} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default function App() {
  return <AuthProvider><BrowserRouter><AppRoutes /></BrowserRouter></AuthProvider>;
}
