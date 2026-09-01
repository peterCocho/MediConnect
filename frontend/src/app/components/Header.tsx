import { Bell, Mail, ChevronDown, LogOut } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import { User } from '../types/user';

interface HeaderProps {
  user: User;
  onLogout: () => void;
}

const roleLabels = {
  ADMIN: 'Administrador',
  DOCTOR: 'Médico',
  RECEPTIONIST: 'Recepcionista',
};

const globalSearchRoutes = [
  { path: '/dashboard', keywords: ['panel', 'inicio', 'dashboard', 'resumen', 'control'] },
  { path: '/pacientes', keywords: ['pacientes', 'paciente'] },
  { path: '/usuarios', keywords: ['usuarios', 'medicos', 'médicos', 'personal', 'usuarios del sistema'] },
  { path: '/recepcionistas', keywords: ['recepcionistas', 'recepcionista'] },
  { path: '/plantillas', keywords: ['plantillas', 'clinicas', 'clínicas', 'template'] },
  { path: '/citas-global', keywords: ['citas global', 'agenda global', 'citas', 'agenda'] },
  { path: '/reportes', keywords: ['reportes', 'reporte', 'estadisticas', 'analytics'] },
  { path: '/agendamiento', keywords: ['agendamiento', 'gestión de citas', 'cita'] },
  { path: '/notificaciones', keywords: ['notificaciones', 'whatsapp', 'mensaje'] },
  { path: '/mi-agenda', keywords: ['mi agenda', 'agenda', 'turnos'] },
  { path: '/historial', keywords: ['historial', 'historial clínico', 'consulta'] },
  { path: '/consulta', keywords: ['consulta', 'nueva consulta'] },
];

export function Header({ user, onLogout }: HeaderProps) {
  const navigate = useNavigate();
  const [searchValue, setSearchValue] = useState('');

  const handleGlobalSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const value = searchValue.trim().toLowerCase();

    if (!value) {
      return;
    }

    const match = globalSearchRoutes.find(({ keywords }) =>
      keywords.some((keyword) => value.includes(keyword.toLowerCase())),
    );

    if (match) {
      navigate(match.path);
      setSearchValue('');
      return;
    }

    navigate('/dashboard');
    setSearchValue('');
  };

  return (
    // Replaced vertical padding with fixed heights and flex centering
    <header className="relative z-40 flex h-16 w-full items-center border-b border-[#E2E8F0] bg-white px-4 sm:px-6 lg:h-[72px] lg:px-8">
      <div className="flex w-full items-center gap-3 pl-12 lg:pl-0 lg:justify-between">
        <form onSubmit={handleGlobalSearch} className="w-full min-w-0 lg:flex-1">
          <input
            type="text"
            value={searchValue}
            onChange={(event) => setSearchValue(event.target.value)}
            placeholder="Buscar módulo..."
            aria-label="Buscar módulo"
            className="mt-2 h-10 w-full min-w-0 rounded-lg border border-[#CBD5E1] bg-[#F8FAFC] px-4 text-sm text-[#1E293B] transition focus:border-[#2C7A7B] focus:outline-none"
          />
        </form>

        <div className="hidden items-center gap-3 lg:flex lg:justify-end">
          <div className="flex items-center gap-2">
            <button className="relative rounded-lg p-2 transition-colors hover:bg-[#F8FAFC]">
              <Bell className="h-5 w-5 text-[#64748B]" />
              <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-[#EF4444]"></span>
            </button>

            <button className="relative rounded-lg p-2 transition-colors hover:bg-[#F8FAFC]">
              <Mail className="h-5 w-5 text-[#64748B]" />
            </button>
          </div>

          <div className="hidden h-8 w-px bg-[#E2E8F0] lg:block"></div>

          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#2C7A7B]">
              <span className="text-sm font-semibold text-white">
                {user.name.split(' ').map((n) => n[0]).join('')}
              </span>
            </div>

            <div className="flex min-w-0 flex-col">
              <span className="truncate text-sm font-medium text-[#1E293B]">{user.name}</span>
              <span className="text-xs text-[#64748B]">{roleLabels[user.role]}</span>
            </div>

            <button className="group rounded transition-colors hover:bg-[#F8FAFC]">
              <ChevronDown className="h-4 w-4 text-[#64748B] group-hover:text-[#1E293B]" />
            </button>

            <button
              onClick={onLogout}
              className="group rounded-lg p-2 transition-colors hover:bg-[#FEE2E2]"
              title="Cerrar sesión"
            >
              <LogOut className="h-5 w-5 text-[#64748B] group-hover:text-[#EF4444]" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}