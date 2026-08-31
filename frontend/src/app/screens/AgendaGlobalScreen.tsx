import { Calendar, Clock, User, Loader2 } from 'lucide-react';
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

export function AgendaGlobalScreen() {
  const [citasGlobal, setCitasGlobal] = useState<AppointmentRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadAgenda = async () => {
      try {
        setIsLoading(true);
        setError('');
        const response = await api.get('/api/appointments', {
          params: {
            page: 0,
            size: 20,
            sort: 'startTime,desc',
            startDate: '2000-01-01T00:00:00Z',
            endDate: '2100-01-01T00:00:00Z',
          },
        });

        setCitasGlobal((response.data?.content ?? []) as AppointmentRow[]);
      } catch (err) {
        setError('No se pudo cargar la agenda global.');
      } finally {
        setIsLoading(false);
      }
    };

    loadAgenda();
  }, []);

  const confirmedCount = citasGlobal.filter((cita) => cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED').length;
  const pendingCount = citasGlobal.filter((cita) => cita.status === 'PENDING_CONFIRMATION').length;

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Agenda Global - Panel de Monitorización</h1>
        <p className="text-[#64748B]">Vista de todas las citas registradas en la base de datos</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Cargando agenda global...</span>
        </div>
      ) : error ? (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-6 text-center text-red-600">{error}</div>
      ) : (
        <>
          <div className="mb-8 grid gap-6 md:grid-cols-3">
            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <Calendar className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Total</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{citasGlobal.length}</h3>
              <p className="text-sm text-[#64748B]">Citas registradas</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#10B981] bg-opacity-20 p-3">
                  <Clock className="h-6 w-6 text-[#10B981]" />
                </div>
                <span className="text-sm text-[#64748B]">Estado</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{confirmedCount}</h3>
              <p className="text-sm text-[#64748B]">Confirmadas</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#F59E0B] bg-opacity-20 p-3">
                  <User className="h-6 w-6 text-[#F59E0B]" />
                </div>
                <span className="text-sm text-[#64748B]">Estado</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{pendingCount}</h3>
              <p className="text-sm text-[#64748B]">Pendientes</p>
            </div>
          </div>

          <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
            <div className="flex items-center justify-between rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
              <h2 className="text-xl font-semibold text-[#1E293B]">Registro completo de citas</h2>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-[760px]">
                <thead className="hidden md:table-header-group">
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Hora</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Paciente</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Médico</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {citasGlobal.map((cita) => {
                    const fecha = new Date(cita.startTime);
                    const hora = fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', hour12: false });
                    const fechaTexto = fecha.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });

                    return (
                      <tr key={cita.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm font-medium text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">
                          <span>{hora}</span>
                          <span className="text-xs text-[#64748B] md:hidden">{fechaTexto}</span>
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Paciente">
                          Paciente ID: {cita.patientId}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Médico">
                          Médico ID: {cita.doctorId}
                        </td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                          <span className={`inline-flex items-center gap-2 text-sm ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'text-[#10B981]' : 'text-[#F59E0B]'}`}>
                            <span className={`h-2 w-2 rounded-full ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}`} />
                            {cita.status}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
