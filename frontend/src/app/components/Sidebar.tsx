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
    id: "recepcionistas",
    label: "Recepcionistas",
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
    <aside className="w-full bg-[#455A73] text-white md:w-64 md:min-h-screen md:flex md:flex-col">
      <div className="border-b border-[#334155] p-4 md:p-6">
        <div className="flex items-center justify-center gap-2 md:justify-start">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#8CD6D1]">
            <MediConnectBrandIcon className="h-5 w-5 text-[#455A73]" />
          </div>
          <h1 className="text-lg font-bold md:text-xl">MediConnect</h1>
        </div>
      </div>

      <nav className="flex gap-2 overflow-x-auto px-3 py-3 md:flex-1 md:flex-col md:overflow-y-auto md:px-4 md:py-4">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeScreen === item.id;

          return (
            <button
              key={item.id}
              onClick={() => onNavigate(item.id)}
              className={`flex min-w-max items-center gap-2 rounded-lg px-3 py-2 text-left transition-all md:w-full md:gap-3 md:px-4 md:py-3 ${
                isActive
                  ? "border-l-0 bg-[#334155] font-semibold text-white md:border-l-4 md:border-[#8CD6D1]"
                  : "text-[#CBD5E1] hover:bg-[#334155] hover:text-white"
              }`}
            >
              <Icon className="h-4 w-4 shrink-0 md:h-5 md:w-5" />
              <span className="text-xs md:text-sm">{item.label}</span>
            </button>
          );
        })}
      </nav>

      <div className="border-t border-[#334155] p-3 md:p-4">
        <div className="text-center text-[10px] text-[#94A3B8] md:text-xs">
          MediConnect v1.0
        </div>
      </div>
    </aside>
  );
}