import {
  Calendar as CalendarIcon,
  UserCheck,
  TrendingDown,
  BarChart3,
  Activity,
  Loader2,
} from "lucide-react";
import { useEffect, useState } from 'react';
import api from '../../service/api';

type AppointmentRow = {
  id: number;
  patientId: number;
  doctorId: number;
  startTime: string;
  endTime: string;
  status: string;
};

type DoctorRow = {
  id: number;
  isActive?: boolean;
  active?: boolean;
};

type DashboardMetrics = {
  totalCompletedConsultations: number;
  topDiagnoses: Array<{ icd10Code: string; occurrences: number }>;
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

export function DashboardScreen() {
  const [appointments, setAppointments] = useState<AppointmentRow[]>([]);
  const [allAppointments, setAllAppointments] = useState<AppointmentRow[]>([]);
  const [activeDoctorsToday, setActiveDoctorsToday] = useState(0);
  const [metrics, setMetrics] = useState<DashboardMetrics>({ totalCompletedConsultations: 0, topDiagnoses: [] });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        setIsLoading(true);
        setError('');

        const [appointmentsResponse, allAppointmentsResponse, doctorsResponse, reportResponse] = await Promise.allSettled([
          api.get('/api/appointments', { params: { page: 0, size: 5, sort: 'startTime,desc' } }),
          api.get('/api/appointments', { params: { page: 0, size: 1000, sort: 'startTime,desc' } }),
          api.get('/api/users/doctors', { params: { page: 0, size: 1000, sortBy: 'id', isActive: true } }),
          api.get('/api/reports/dashboard'),
        ]);

        const recentAppointments = appointmentsResponse.status === 'fulfilled' ? appointmentsResponse.value.data?.content ?? [] : [];
        const completeAppointments = (allAppointmentsResponse.status === 'fulfilled' ? allAppointmentsResponse.value.data?.content ?? [] : []) as AppointmentRow[];
        const activeDoctors = (doctorsResponse.status === 'fulfilled' ? doctorsResponse.value.data?.content ?? [] : []) as DoctorRow[];
        const reportMetrics = reportResponse.status === 'fulfilled' ? reportResponse.value.data : null;
        setAppointments(recentAppointments as AppointmentRow[]);
        const today = getRelativeDateKey(0);
        const doctorsWithAppointments = new Set(
          completeAppointments
            .filter((appointment) => getDateKey(appointment.startTime) === today && appointment.status === 'SCHEDULED')
            .map((appointment) => appointment.doctorId),
        );
        setAllAppointments(completeAppointments);
        setActiveDoctorsToday(activeDoctors.filter((doctor) => doctorsWithAppointments.has(doctor.id)).length);
        setMetrics((reportMetrics ?? { totalCompletedConsultations: 0, topDiagnoses: [] }) as DashboardMetrics);
      } catch (err) {
        setError('No se pudo cargar el panel del sistema.');
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboard();
  }, []);

// Calculate date boundaries once
  const now = new Date();
  const currentMonth = now.getMonth();
  const currentYear = now.getFullYear();

  // Filter appointments for the current month
  const monthlyAppointments = allAppointments.filter((appointment) => {
    const appointmentDate = new Date(appointment.startTime);
    return appointmentDate.getMonth() === currentMonth && appointmentDate.getFullYear() === currentYear;
  });

  // Calculate rate using the array length directly
  const totalMonthly = monthlyAppointments.length;
  const cancelledAppointments = monthlyAppointments.filter((appointment) => appointment.status === 'CANCELED').length;
  const cancellationRate = totalMonthly > 0 ? Math.round((cancelledAppointments / totalMonthly) * 100) : 0;

  // 2. Calculate Completed Consultations (Last 7 Days)
  const weekStart = new Date();
  weekStart.setHours(0, 0, 0, 0);
  weekStart.setDate(weekStart.getDate() - 6);
  
  const weekCompletedCount = allAppointments.filter((appointment) => 
    appointment.status === 'COMPLETED' && new Date(appointment.startTime) >= weekStart
  ).length;

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Panel de Control</h1>
        <p className="text-[#64748B]">Resumen de actividad del sistema</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Cargando resumen...</span>
        </div>
      ) : error ? (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-6 text-center text-red-600">{error}</div>
      ) : (
        <>
          <div className="mb-8 grid gap-6 sm:grid-cols-2 xl:grid-cols-4">
            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <TrendingDown className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Este mes</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{cancellationRate}%</h3>
              <p className="text-sm text-[#64748B]">Tasa de cancelación</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <UserCheck className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Hoy</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{activeDoctorsToday}</h3>
              <p className="text-sm text-[#64748B]">Doctores activos hoy</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <BarChart3 className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Últimos 7 días</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{weekCompletedCount}</h3>
              <p className="text-sm text-[#64748B]">Consultas completadas</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <Activity className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Diagnósticos más frecuentes</span>
              </div>
              {metrics.topDiagnoses?.length ? (
                <div className="space-y-2">
                  {metrics.topDiagnoses.slice(0, 3).map((diagnosis) => (
                    <div key={diagnosis.icd10Code} className="flex items-center justify-between gap-3">
                      <span className="text-sm font-semibold text-[#1E293B]">{diagnosis.icd10Code}</span>
                      <span className="text-xs text-[#64748B]">{diagnosis.occurrences} {diagnosis.occurrences === 1 ? 'caso' : 'casos'}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-[#64748B]">Sin diagnósticos registrados</p>
              )}
            </div>
          </div>

          <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
            <div className="rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
              <h2 className="text-xl font-semibold text-[#1E293B]">Citas del sistema</h2>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-0 md:min-w-[640px]">
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
                      <td colSpan={4} className="px-6 py-10 text-center text-[#64748B]">No hay citas registradas.</td>
                    </tr>
                  ) : (
                          appointments.map((cita) => (
                            
                      
                      <tr key={cita.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                                                <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Fecha">
                          {new Date(cita.startTime).toLocaleDateString('es-ES')}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">
                          {new Date(cita.startTime).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', hour12: false })}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Paciente">Paciente #{cita.patientId}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Médico">Médico #{cita.doctorId}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                          <span className={`inline-flex items-center gap-2 text-sm ${cita.status === 'SCHEDULED' ? 'text-[#10B981]' : 'text-[#F59E0B]'}`}>
                            <span className={`h-2 w-2 rounded-full ${cita.status === 'SCHEDULED' ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}`} />
                            {cita.status}
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