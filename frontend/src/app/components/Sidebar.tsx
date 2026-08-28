import {
  Home,
  Users,
  Calendar,
  FileText,
  UserCog,
  Settings,
  BarChart3,
  Bell,
  Stethoscope,
  LayoutTemplate,
} from "lucide-react";
import { UserRole } from "../types/user";

// Custom geometric abstract logo component
const MediConnectBrandIcon = ({ className = "w-5 h-5 text-[#455A73]" }) => (
  <svg
    className={className}
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M4 19V7C4 5.34315 5.34315 4 7 4H8.5L12 10L15.5 4H17C18.6569 4 20 5.34315 20 7V19"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <circle cx="12" cy="10" r="3" fill="currentColor" />
    <path
      d="M12 13V20"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
    />
  </svg>
);

interface SidebarProps {
  activeScreen: string;
  onNavigate: (screen: string) => void;
  userRole: UserRole;
}

interface MenuItem {
  id: string;
  label: string;
  icon: any;
  roles: UserRole[];
}

const allMenuItems: MenuItem[] = [
  {
    id: "dashboard",
    label: "Panel de Control",
    icon: Home,
    roles: ["ADMIN", "DOCTOR", "RECEPTIONIST"],
  },

  // Admin específico
  {
    id: "usuarios",
    label: "Gestión de Usuarios",
    icon: UserCog,
    roles: ["ADMIN"],
  },
  {
    id: "plantillas",
    label: "Plantillas Clínicas",
    icon: LayoutTemplate,
    roles: ["ADMIN"],
  },
  {
    id: "citas-global",
    label: "Citas Global",
    icon: Calendar,
    roles: ["ADMIN"],
  },
  {
    id: "reportes",
    label: "Reportes",
    icon: BarChart3,
    roles: ["ADMIN"],
  },

  // Doctor específico
  {
    id: "mi-agenda",
    label: "Mi Agenda",
    icon: Calendar,
    roles: ["DOCTOR"],
  },
  {
    id: "pacientes",
    label: "Mis Pacientes",
    icon: Users,
    roles: ["DOCTOR"],
  },
  {
    id: "historial",
    label: "Historial Clínico",
    icon: FileText,
    roles: ["DOCTOR"],
  },
  {
    id: "consulta",
    label: "Nueva Consulta",
    icon: Stethoscope,
    roles: ["DOCTOR"],
  },

  // Recepcionista específico
  {
    id: "pacientes",
    label: "Registrar Paciente",
    icon: Users,
    roles: ["RECEPTIONIST"],
  },
  {
    id: "agendamiento",
    label: "Gestión de Citas",
    icon: Calendar,
    roles: ["RECEPTIONIST"],
  },
  {
    id: "notificaciones",
    label: "Notificaciones",
    icon: Bell,
    roles: ["RECEPTIONIST"],
  },
];

export function Sidebar({
  activeScreen,
  onNavigate,
  userRole,
}: SidebarProps) {
  const menuItems = allMenuItems.filter((item) =>
    item.roles.includes(userRole),
  );

  return (
    <div className="w-64 bg-[#455A73] h-screen flex flex-col">
      <div className="p-6 border-b border-[#334155]">
        <div className="flex items-center gap-2">
          {/* Replace generic '+' with the custom abstract logo */}
          <div className="w-8 h-8 bg-[#8CD6D1] rounded-lg flex items-center justify-center">
            <MediConnectBrandIcon className="w-5 h-5 text-[#455A73]" />
          </div>
          <h1 className="text-white text-xl font-bold">
            MediConnect
          </h1>
        </div>
      </div>

      <nav className="flex-1 px-4 py-4 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeScreen === item.id;

          return (
            <button
              key={item.id}
              onClick={() => onNavigate(item.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg mb-1 transition-all ${
                isActive
                  ? "bg-[#334155] border-l-4 border-[#8CD6D1] text-white font-semibold"
                  : "text-[#CBD5E1] hover:bg-[#334155] hover:text-white"
              }`}
            >
              <Icon className="w-5 h-5" />
              <span className="text-sm">{item.label}</span>
            </button>
          );
        })}
      </nav>

      <div className="p-4 border-t border-[#334155]">
        <div className="text-[#94A3B8] text-xs text-center">
          MediConnect v1.0
        </div>
      </div>
    </div>
  );
}