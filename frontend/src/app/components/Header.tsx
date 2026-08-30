import { Bell, Mail, ChevronDown, LogOut } from 'lucide-react';
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

export function Header({ user, onLogout }: HeaderProps) {
  return (
    <header className="border-b border-[#E2E8F0] bg-white px-4 py-3 sm:px-6 lg:px-8 lg:py-4">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div className="w-full lg:flex-1">
          <input
            type="text"
            placeholder="Búsqueda global..."
            className="w-full rounded-lg border border-[#CBD5E1] bg-[#F8FAFC] px-4 py-2 text-sm text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none lg:w-96"
          />
        </div>

        <div className="flex flex-wrap items-center justify-between gap-3 lg:justify-end">
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

            <button className="rounded transition-colors hover:bg-[#F8FAFC] group">
              <ChevronDown className="h-4 w-4 text-[#64748B] group-hover:text-[#1E293B]" />
            </button>

            <button
              onClick={onLogout}
              className="rounded-lg p-2 transition-colors hover:bg-[#FEE2E2] group"
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