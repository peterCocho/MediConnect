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
import { Icon } from '@iconify/react';
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

// --- ICONS UPDATED WITH ICONIFY ---
// Uses 'mdi:doctor' for doctors
const DoctorIcon = ({ className }: { className?: string }) => (
  <Icon icon="mdi:doctor" className={className} />
);

// Uses 'mdi:headset' for receptionists as the most reliable standard
// Alternatives include 'mdi:account-tie' and 'mdi:counter'
const ReceptionistIcon = ({ className }: { className?: string }) => (
  <Icon icon="mdi:headset" className={className} />
);

export interface SidebarProps {
  activeScreen: string;
  onNavigate: (screen: string) => void;
  userRole: UserRole;
  isOpen?: boolean;
  onClose?: () => void;
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

  // Administrator-specific navigation
  {
    id: "usuarios",
    label: "Doctores",
    icon: DoctorIcon, // Doctor icon
    roles: ["ADMIN"],
  },
  {
    id: "recepcionistas",
    label: "Recepcionistas",
    icon: ReceptionistIcon, // Receptionist icon
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
    roles: ["ADMIN", "RECEPTIONIST"],
  },
  {
    id: "reportes",
    label: "Reportes",
    icon: BarChart3,
    roles: ["ADMIN"],
  },

  // Doctor-specific navigation
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

  // Receptionist-specific navigation
  {
    id: "pacientes",
    label: "Pacientes",
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
  }
];

export function Sidebar({
  activeScreen,
  onNavigate,
  userRole,
  isOpen = false,
  onClose,
}: SidebarProps) {
  const menuItems = allMenuItems.filter((item) =>
    item.roles.includes(userRole),
  );

  return (
    <aside className={`fixed inset-y-0 left-0 z-50 w-64 transform transition-transform duration-300 bg-[#455A73] text-white flex flex-col ${isOpen ? 'translate-x-0' : '-translate-x-full'} lg:static lg:z-auto lg:translate-x-0 lg:shadow-none ${isOpen ? 'lg:translate-x-0' : ''}`}>
      <div className="border-b border-[#334155] p-4 md:p-6">
        <button 
          type="button"
          onClick={() => {
            onNavigate("dashboard");
            if (onClose) onClose();
          }}
          className="flex w-full items-center justify-center gap-2 transition-opacity hover:opacity-80 md:justify-start"
        >
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[#8CD6D1]">
            <MediConnectBrandIcon className="h-5 w-5 text-[#455A73]" />
          </div>
          <h1 className="text-lg font-bold md:text-xl">MediConnect</h1>
        </button>
      </div>

      <nav className="flex-1 flex flex-col gap-2 overflow-y-auto px-3 py-3 md:px-4 md:py-4">
        {menuItems.map((item) => {
          const IconComponent = item.icon;
          const isActive = activeScreen === item.id;

          return (
            <button
              key={item.id}
              onClick={() => {
                onNavigate(item.id);
                if (onClose) onClose();
              }}
              className={`flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left transition-all md:gap-3 md:px-4 md:py-3 ${
                isActive
                  ? "border-l-0 bg-[#334155] font-semibold text-white md:border-l-4 md:border-[#8CD6D1]"
                  : "text-[#CBD5E1] hover:bg-[#334155] hover:text-white"
              }`}
            >
              <IconComponent className="h-4 w-4 shrink-0 md:h-5 md:w-5" />
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