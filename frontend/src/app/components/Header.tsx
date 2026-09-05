import { Bell, LogOut, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import api from '../../service/api';
import { User } from '../types/user';

interface HeaderProps {
  user: User;
  onLogout: () => void;
}

type AlertItem = {
  id: number;
  title: string;
  subtitle: string;
  path: string;
  actionLabel: string;
};

type SearchRoute = {
  path: string;
  label: string;
  keywords: string[];
};

const roleLabels = {
  ADMIN: 'Administrador',
  DOCTOR: 'Médico',
  RECEPTIONIST: 'Recepcionista',
};

const getGlobalSearchRoutesByRole = (role: User['role']) => {
  const baseRoutes = [
    { path: '/dashboard', label: 'Dashboard', keywords: ['panel', 'inicio', 'dashboard', 'resumen', 'control'] },
    { path: '/pacientes', label: 'Pacientes', keywords: ['pacientes', 'paciente', 'historial médico'] },
    { path: '/usuarios', label: 'Usuarios', keywords: ['usuarios', 'medicos', 'médicos', 'personal', 'usuarios del sistema', 'doctores'] },
    { path: '/recepcionistas', label: 'Recepcionistas', keywords: ['recepcionistas', 'recepcionista'] },
    { path: '/plantillas', label: 'Plantillas', keywords: ['plantillas', 'clinicas', 'clínicas', 'template', 'plantilla'] },
    { path: '/citas-global', label: 'Agenda global', keywords: ['citas global', 'agenda global', 'citas', 'agenda', 'turnos'] },
    { path: '/reportes', label: 'Reportes', keywords: ['reportes', 'reporte', 'estadisticas', 'analytics', 'estadística'] },
    { path: '/agendamiento', label: 'Agendamiento', keywords: ['agendamiento', 'gestión de citas', 'cita', 'reservar cita'] },
    { path: '/notificaciones', label: 'Notificaciones', keywords: ['notificaciones', 'whatsapp', 'mensaje', 'mensajes'] },
    { path: '/mi-agenda', label: 'Mi agenda', keywords: ['mi agenda', 'agenda del doctor', 'turnos del médico', 'mi turno'] },
    { path: '/historial', label: 'Historial clínico', keywords: ['historial', 'historial clínico', 'consulta', 'expediente'] },
    { path: '/consulta', label: 'Consulta', keywords: ['consulta', 'nueva consulta', 'atender paciente'] },
  ];

  if (role === 'ADMIN') {
    return baseRoutes.filter((route) => !['/pacientes', '/agendamiento', '/notificaciones', '/mi-agenda', '/historial', '/consulta'].includes(route.path));
  }

  if (role === 'DOCTOR') {
    return baseRoutes.filter((route) => !['/recepcionistas', '/plantillas', '/citas-global', '/reportes', '/agendamiento', '/notificaciones'].includes(route.path));
  }

  return baseRoutes.filter((route) => !['/usuarios', '/recepcionistas', '/plantillas', '/reportes', '/mi-agenda', '/historial', '/consulta'].includes(route.path));
};

export function Header({ user, onLogout }: HeaderProps) {
  const navigate = useNavigate();
  const [searchValue, setSearchValue] = useState('');
  const [searchSuggestions, setSearchSuggestions] = useState<SearchRoute[]>([]);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isAlertOpen, setIsAlertOpen] = useState(false);
  const [alertItems, setAlertItems] = useState<AlertItem[]>([]);
  const [isLoadingAlerts, setIsLoadingAlerts] = useState(false);

  const shouldShowBell = user.role === 'DOCTOR' || user.role === 'RECEPTIONIST';

  useEffect(() => {
    if (!shouldShowBell) {
      setAlertItems([]);
      return;
    }

    const loadAlerts = async () => {
      try {
        setIsLoadingAlerts(true);

        if (user.role === 'RECEPTIONIST') {
          const response = await api.get('/api/whatsapp/messages/unread');
          const items = (response.data ?? []).slice(0, 5).map((message: any) => ({
            id: message.id,
            title: 'Mensaje nuevo de WhatsApp',
            subtitle: `${message.phoneNumber} · ${new Date(message.receivedAt).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' })}`,
            path: '/notificaciones',
            actionLabel: 'Ver notificaciones',
          }));
          setAlertItems(items);
          return;
        }

        const isToday = (date: string) => {
      const consultationDate = new Date(date);
      const today = new Date();
      return (
        consultationDate.getFullYear() === today.getFullYear() &&
        consultationDate.getMonth() === today.getMonth() &&
        consultationDate.getDate() === today.getDate()
      );
    };

    const response = await api.get('/api/consultations', {
      params: { page: 0, size: 5, sort: 'consultationDate,asc' },
    });

    const items = (response.data?.content ?? [])
      .filter((consultation: any) => consultation.status === 'SCHEDULED' && isToday(consultation.consultationDate))
      .slice(0, 5)
      .map((consultation: any) => ({
        id: consultation.id,
        title: `Consulta programada`,
        subtitle: `${new Date(consultation.consultationDate).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' })}`,
        path: '/mi-agenda',
        actionLabel: 'Ir a mi agenda',
      }));

        setAlertItems(items);
      } catch (error) {
        setAlertItems([]);
      } finally {
        setIsLoadingAlerts(false);
      }
    };

    loadAlerts();
  }, [shouldShowBell, user.role]);

  const notificationCount = alertItems.length;

  const getSearchSuggestions = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) {
      return [];
    }

    const query = trimmed.toLowerCase();
    const availableRoutes = getGlobalSearchRoutesByRole(user.role);

    return availableRoutes.filter(({ keywords, label }) => {
      const haystack = [...keywords, label.toLowerCase()].join(' ');
      return haystack.includes(query) || query.includes(label.toLowerCase()) || keywords.some((keyword) => keyword.includes(query));
    }).slice(0, 5);
  };

  const handleGlobalSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const value = searchValue.trim();

    if (!value) {
      return;
    }

    const suggestions = getSearchSuggestions(value);
    const target = suggestions[0] ?? getGlobalSearchRoutesByRole(user.role)[0];

    navigate(target.path);
    setSearchValue('');
    setSearchSuggestions([]);
    setIsSearchOpen(false);
  };

  const openAlerts = () => {
    if (!shouldShowBell) {
      return;
    }
    setIsAlertOpen((current) => !current);
  };

  return (
    <>
      <header className="relative z-40 flex h-16 w-full items-center border-b border-[#E2E8F0] bg-white px-4 sm:px-6 lg:h-[72px] lg:px-8">
        <div className="flex w-full items-center gap-2 pl-12 lg:pl-0 lg:gap-3">
          <form onSubmit={handleGlobalSearch} className="relative min-w-0 flex-1">
            <input
              type="text"
              value={searchValue}
              onChange={(event) => {
                const nextValue = event.target.value;
                setSearchValue(nextValue);
                setSearchSuggestions(getSearchSuggestions(nextValue));
                setIsSearchOpen(nextValue.trim().length > 0);
              }}
              onFocus={() => {
                if (searchValue.trim()) {
                  setIsSearchOpen(true);
                  setSearchSuggestions(getSearchSuggestions(searchValue));
                }
              }}
              onBlur={() => {
                window.setTimeout(() => {
                  setIsSearchOpen(false);
                  setSearchSuggestions([]);
                }, 120);
              }}
              placeholder="Buscar módulo, paciente o agenda..."
              aria-label="Buscar módulo, paciente o agenda"
              className="h-10 mt-2 w-full min-w-0 rounded-lg border border-[#CBD5E1] bg-[#F8FAFC] px-4 text-sm text-[#1E293B] transition focus:border-[#2C7A7B] focus:outline-none"
            />

            {isSearchOpen && searchSuggestions.length > 0 && (
              <div className="absolute left-0 right-0 top-[calc(100%+0.5rem)] z-50 overflow-hidden rounded-xl border border-[#E2E8F0] bg-white shadow-xl">
                {searchSuggestions.map((suggestion) => (
                  <button
                    key={suggestion.path}
                    type="button"
                    onMouseDown={(event) => {
                      event.preventDefault();
                      navigate(suggestion.path);
                      setSearchValue('');
                      setSearchSuggestions([]);
                      setIsSearchOpen(false);
                    }}
                    className="flex w-full items-center justify-between gap-3 border-b border-[#F1F5F9] px-4 py-3 text-left transition hover:bg-[#F8FAFC] last:border-b-0"
                  >
                    <div>
                      <div className="text-sm font-medium text-[#1E293B]">{suggestion.label}</div>
                      <div className="text-xs text-[#64748B]">Ir al módulo</div>
                    </div>
                    <span className="rounded-md bg-[#ECFDF5] px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-[#047857]">
                      acceso
                    </span>
                  </button>
                ))}
              </div>
            )}
          </form>

          <div className="flex shrink-0 items-center gap-1 sm:gap-2">
            {shouldShowBell && (
              <div className="relative">
                <button
                  type="button"
                  onClick={openAlerts}
                  className="relative rounded-lg p-2 transition-colors hover:bg-[#F8FAFC]"
                  aria-label="Notificaciones"
                >
                  <Bell className="h-5 w-5 text-[#64748B]" />
                  {notificationCount > 0 && (
                    <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-[#EF4444] px-1 text-[10px] font-bold text-white">
                      {notificationCount > 9 ? '9+' : notificationCount}
                    </span>
                  )}
                </button>
              </div>
            )}

            <div className="hidden h-8 w-px bg-[#E2E8F0] md:block"></div>

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
            </div>

            <button
              type="button"
              onClick={onLogout}
              className="rounded-lg p-2 transition-colors hover:bg-[#FEE2E2]"
              title="Cerrar sesión"
              aria-label="Cerrar sesión"
            >
              <LogOut className="h-5 w-5 text-[#64748B]" />
            </button>
          </div>
        </div>
      </header>

      {isAlertOpen && shouldShowBell && (
        <div className="absolute right-4 top-16 z-50 w-[min(24rem,calc(100vw-2rem))] rounded-xl border border-[#E2E8F0] bg-white p-4 shadow-xl lg:right-8 lg:top-[72px]">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-[#1E293B]">Notificaciones</h2>
            <button
              type="button"
              onClick={() => setIsAlertOpen(false)}
              className="rounded-md p-1 text-[#64748B] hover:bg-[#F8FAFC]"
              aria-label="Cerrar notificaciones"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
          {isLoadingAlerts ? (
            <p className="text-sm text-[#64748B]">Cargando...</p>
          ) : alertItems.length === 0 ? (
            <p className="text-sm text-[#64748B]">No hay notificaciones nuevas.</p>
          ) : (
            <div className="space-y-2">
              {alertItems.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => {
                    navigate(item.path);
                    setIsAlertOpen(false);
                  }}
                  className="w-full rounded-lg bg-[#F8FAFC] p-3 text-left hover:bg-[#F1F5F9]"
                >
                  <div className="text-sm font-medium text-[#1E293B]">{item.title}</div>
                  <div className="mt-1 text-xs text-[#64748B]">{item.subtitle}</div>
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </>
  );
}