import {
  Calendar as CalendarIcon,
  Clock,
  Users,
  Loader2,
} from 'lucide-react';
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

type PatientSummary = {
  id: number;
  fullName?: string;
  identityDocument?: string;
};

export function ReceptionistDashboard() {
  const [appointments, setAppointments] = useState<AppointmentRow[]>([]);
  const [patients, setPatients] = useState<PatientSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadReceptionistDashboard = async () => {
      try {
        setIsLoading(true);
        setError('');

        const [appointmentsResponse, patientsResponse] = await Promise.all([
          api.get('/api/appointments', {
            params: {
              page: 0,
              size: 5,
              sort: 'startTime,desc',
              startDate: '2000-01-01T00:00:00Z',
              endDate: '2100-01-01T00:00:00Z',
            },
          }),
          api.get('/api/patients', { params: { page: 0, size: 5, sortBy: 'id' } }),
        ]);

        setAppointments((appointmentsResponse.data?.content ?? []) as AppointmentRow[]);
        setPatients((patientsResponse.data?.content ?? []) as PatientSummary[]);
      } catch (err) {
        setError('No se pudo cargar el panel del recepcionista.');
      } finally {
        setIsLoading(false);
      }
    };

    loadReceptionistDashboard();
  }, []);

  const upcomingCount = appointments.filter((cita) => cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED').length;

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
          <div className="mb-8 grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <CalendarIcon className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Agenda</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{appointments.length}</h3>
              <p className="text-sm text-[#64748B]">Citas recientes</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <Clock className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Estado</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{upcomingCount}</h3>
              <p className="text-sm text-[#64748B]">Programadas</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <Users className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Pacientes</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{patients.length}</h3>
              <p className="text-sm text-[#64748B]">Registrados</p>
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
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">
                          {new Date(cita.startTime).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', hour12: false })}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Paciente">Paciente #{cita.patientId}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Médico">Médico #{cita.doctorId}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                          <span className={`inline-flex items-center gap-2 text-sm ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'text-[#10B981]' : 'text-[#F59E0B]'}`}>
                            <span className={`h-2 w-2 rounded-full ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}`} />
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
