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
    <header className="bg-white border-b border-[#E2E8F0] px-8 py-4">
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Búsqueda global..."
            className="w-96 px-4 py-2 bg-[#F8FAFC] border border-[#CBD5E1] rounded-lg text-sm text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
          />
        </div>

        <div className="flex items-center gap-4">
          <button className="relative p-2 hover:bg-[#F8FAFC] rounded-lg transition-colors">
            <Bell className="w-5 h-5 text-[#64748B]" />
            <span className="absolute top-1 right-1 w-2 h-2 bg-[#EF4444] rounded-full"></span>
          </button>

          <button className="relative p-2 hover:bg-[#F8FAFC] rounded-lg transition-colors">
            <Mail className="w-5 h-5 text-[#64748B]" />
          </button>

          <div className="h-8 w-px bg-[#E2E8F0]"></div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-[#2C7A7B] rounded-full flex items-center justify-center">
              <span className="text-white font-semibold text-sm">
                {user.name.split(' ').map(n => n[0]).join('')}
              </span>
            </div>

            <div className="flex flex-col">
              <span className="text-[#1E293B] text-sm font-medium">{user.name}</span>
              <span className="text-[#64748B] text-xs">{roleLabels[user.role]}</span>
            </div>

            <button className="p-1 hover:bg-[#F8FAFC] rounded transition-colors group">
              <ChevronDown className="w-4 h-4 text-[#64748B] group-hover:text-[#1E293B]" />
            </button>

            <button
              onClick={onLogout}
              className="ml-2 p-2 hover:bg-[#FEE2E2] rounded-lg transition-colors group"
              title="Cerrar sesión"
            >
              <LogOut className="w-5 h-5 text-[#64748B] group-hover:text-[#EF4444]" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}