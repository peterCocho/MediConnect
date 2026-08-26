// @ts-nocheck
import { useState } from "react";
import { Sidebar } from "./components/Sidebar";
import { Header } from "./components/Header";
import { LoginScreen } from "./screens/LoginScreen";
import { DashboardScreen } from "./screens/DashboardScreen";
import { PacientesScreen } from "./screens/PacientesScreen";
import { UsuariosScreen } from "./screens/UsuariosScreen";
import { AgendamientoScreen } from "./screens/AgendamientoScreen";
import { HistorialScreen } from "./screens/HistorialScreen";
import { ConsultaScreen } from "./screens/ConsultaScreen";
import { ReportesScreen } from "./screens/ReportesScreen";
import { NotificacionesScreen } from "./screens/NotificacionesScreen";
import { RegistroPacienteScreen } from "./screens/RegistroPacienteScreen";
import { AgendaGlobalScreen } from "./screens/AgendaGlobalScreen";
import { MiAgendaScreen } from "./screens/MiAgendaScreen";
import { MisPacientesScreen } from "./screens/MisPacientesScreen";
import { AdministracionPlantillasScreen } from "./screens/AdministracionPlantillasScreen";
import { User } from "./types/user";

export default function App() {
  const [currentUser, setCurrentUser] = useState<User | null>(
    null,
  );
  const [activeScreen, setActiveScreen] = useState("dashboard");

  if (!currentUser) {
    return (
      <LoginScreen onLogin={(user) => setCurrentUser(user)} />
    );
  }

  const handleLogout = () => {
    setCurrentUser(null);
    setActiveScreen("dashboard");
  };

  const renderScreen = () => {
    switch (activeScreen) {
      case "dashboard":
        return <DashboardScreen />;
      case "pacientes":
        if (currentUser.role === "RECEPTIONIST")
          return <RegistroPacienteScreen />;
        if (currentUser.role === "DOCTOR")
          return (
            <MisPacientesScreen
              onVerHistorial={() =>
                setActiveScreen("historial")
              }
            />
          );
        return <PacientesScreen />;
      case "usuarios":
        return <UsuariosScreen />;
      case "plantillas":
        return <AdministracionPlantillasScreen />;
      case "agendamiento":
        return <AgendamientoScreen />;
      case "mi-agenda":
        return (
          <MiAgendaScreen
            onIniciarConsulta={() =>
              setActiveScreen("consulta")
            }
          />
        );
      case "citas-global":
        return <AgendaGlobalScreen />;
      case "historial":
        return <HistorialScreen />;
      case "consulta":
        return <ConsultaScreen />;
      case "reportes":
        return <ReportesScreen />;
      case "notificaciones":
        return <NotificacionesScreen />;
      default:
        return <DashboardScreen />;
    }
  };

  return (
    <div className="flex h-screen bg-[#F4F7F9]">
      <Sidebar
        activeScreen={activeScreen}
        onNavigate={setActiveScreen}
        userRole={currentUser.role}
      />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header user={currentUser} onLogout={handleLogout} />
        <div className="flex-1 overflow-auto">
          {renderScreen()}
        </div>
      </div>
    </div>
  );
}