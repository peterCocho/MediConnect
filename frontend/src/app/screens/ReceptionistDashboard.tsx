import {
  Calendar as CalendarIcon,
  UserPlus,
  BellRing,
  MessageCircle,
  CalendarX2,
  Loader2,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';
import { translateStatus } from '../utils/statusLabels';

type AppointmentRow = {
  id: number;
  patientId: number;
  doctorId: number;
  startTime: string;
  endTime: string;
  status: string;
};

type PatientSummary = {
  id: number;
  fullName?: string;
  identityDocument?: string;
  createdAt?: string;
};

type ReceptionistMetrics = {
  arrivalsToday: number;
  patientsScheduledToday: number;
  confirmationRateToday: number;
  unreadWhatsapp: number;
  releasedSlotsToday: number;
};

const getDateKey = (value: string | Date) => {
  const date = value instanceof Date ? value : new Date(value);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const getRelativeDateKey = (daysFromToday: number) => {
  const date = new Date();
  date.setHours(12, 0, 0, 0);
  date.setDate(date.getDate() + daysFromToday);
  return getDateKey(date);
};

export function ReceptionistDashboard() {
  const [appointments, setAppointments] = useState<AppointmentRow[]>([]);
  const [patients, setPatients] = useState<PatientSummary[]>([]);
  const [metrics, setMetrics] = useState<ReceptionistMetrics>({ arrivalsToday: 0, patientsScheduledToday: 0, confirmationRateToday: 0, unreadWhatsapp: 0, releasedSlotsToday: 0 });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

useEffect(() => {
    const loadReceptionistDashboard = async () => {
      try {
        setIsLoading(true);
        setError('');

        // Calculate today's boundaries for backend filtering
        const now = new Date();
        const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0).toISOString();
        const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59).toISOString();

const [recentAppointmentsRes, todayAppointmentsRes, notificationsRes] = await Promise.all([
          // Fetch only 5 recent appointments for the table
          api.get('/api/appointments', {
            params: {
              page: 0,
              size: 5,
              sort: 'startTime,desc',
              startDate: '2020-01-01T00:00:00Z',
              endDate: '2100-01-01T00:00:00Z', 
            },
          }),
          // Fetch strictly today's appointments for metrics
          api.get('/api/appointments', {
            params: {
              page: 0,
              size: 100, 
              sort: 'startTime,asc',
              startDate: startOfDay,
              endDate: endOfDay,
            },
          }),
          api.get('/api/whatsapp/messages/unread'),
        ]);

        setAppointments((recentAppointmentsRes.data?.content ?? []) as AppointmentRow[]);
        const todayAppointments = (todayAppointmentsRes.data?.content ?? []) as AppointmentRow[];
        
        // Isolate states to prevent redundant counts
        const confirmed = todayAppointments.filter(a => a.status === 'CONFIRMED' || a.status === 'SCHEDULED').length;
        const pending = todayAppointments.filter(a => a.status === 'PENDING_CONFIRMATION').length;
        const canceled = todayAppointments.filter(a => a.status === 'CANCELED').length;
        const totalValid = confirmed + pending;

        setMetrics({
          arrivalsToday: confirmed, 
          patientsScheduledToday: totalValid,
          confirmationRateToday: totalValid > 0 ? Math.round((confirmed / totalValid) * 100) : 0,
          unreadWhatsapp: notificationsRes.data?.length ?? 0,
          releasedSlotsToday: canceled,
        });
      } catch (err) {
        setError('No se pudo cargar el panel de recepción.');
      } finally {
        setIsLoading(false);
      }
    };

    loadReceptionistDashboard();
  }, []);

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Panel de Recepción</h1>
        <p className="text-[#64748B]">Resumen de citas y pacientes del sistema</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Cargando panel...</span>
        </div>
      ) : error ? (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-6 text-center text-red-600">{error}</div>
      ) : (
        <>
          <div className="mb-8 grid gap-6 sm:grid-cols-2 xl:grid-cols-5">
            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <CalendarIcon className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Hoy</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics.arrivalsToday}</h3>
              <p className="text-sm text-[#64748B]">Llegadas programadas</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <UserPlus className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Carga de hoy</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics.patientsScheduledToday}</h3>
              <p className="text-sm text-[#64748B]">Pacientes agendados hoy</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <BellRing className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Hoy</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics.confirmationRateToday}%</h3>
              <p className="text-sm text-[#64748B]">Tasa de confirmación diaria</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <MessageCircle className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">WhatsApp</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics.unreadWhatsapp}</h3>
              <p className="text-sm text-[#64748B]">Mensajes sin leer</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <CalendarX2 className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Hoy</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics.releasedSlotsToday}</h3>
              <p className="text-sm text-[#64748B]">Espacios liberados</p>
            </div>
          </div>

          <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
            <div className="rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
              <h2 className="text-xl font-semibold text-[#1E293B]">Citas recientes</h2>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-0">
                <thead className="hidden md:table-header-group">
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Fecha</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Hora</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Paciente</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Médico</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-10 text-center text-[#64748B]">No hay citas registradas.</td>
                    </tr>
                  ) : (
                    appointments.map((cita) => (
                      <tr key={cita.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Fecha" >
                          {new Date(cita.startTime).toLocaleDateString('es-ES', { 
                            day: '2-digit', 
                            month: '2-digit', 
                            year: 'numeric',
                            timeZone: 'America/Bogota' // Timezone correction
                          })}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">
                          {new Date(cita.startTime).toLocaleTimeString('es-ES', { 
                            hour: '2-digit', 
                            minute: '2-digit', 
                            hour12: false,
                            timeZone: 'America/Bogota' // Timezone correction
                          })}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Paciente">Paciente #{cita.patientId}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Médico">Médico #{cita.doctorId}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                          <span className={`inline-flex items-center gap-2 text-sm ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'text-[#10B981]' : 'text-[#F59E0B]'}`}>
                            <span className={`h-2 w-2 rounded-full ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}`} />
                            {translateStatus(cita.status)}
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
