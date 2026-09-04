import { Calendar, Clock, User, Loader2, ChevronLeft, ChevronRight, XCircle, X, AlertTriangle } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';
import { useAuth } from '../context/AuthContext';

type AppointmentRow = {
  id: number;
  patientId: number;
  doctorId: number;
  startTime: string;
  endTime: string;
  status: string;
};

export function AgendaGlobalScreen() {
  const { user } = useAuth();
  const [citasGlobal, setCitasGlobal] = useState<AppointmentRow[]>([]);
  const [allCitas, setAllCitas] = useState<AppointmentRow[]>([]);
  const [totalCitas, setTotalCitas] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [appointmentToCancel, setAppointmentToCancel] = useState<AppointmentRow | null>(null);
  const [cancellationReason, setCancellationReason] = useState('');

  const fetchAgenda = async (nextPage = 0) => {
    try {
      setIsLoading(true);
      setError('');
      const [response, allAppointmentsResponse] = await Promise.all([
        api.get('/api/appointments', {
          params: {
            page: nextPage,
            size: 10,
            sort: 'startTime,desc',
          },
        }),
        api.get('/api/appointments', {
          params: {
            page: 0,
            size: 1000,
            sort: 'startTime,desc',
          },
        }),
      ]);

      setCitasGlobal((response.data?.content ?? []) as AppointmentRow[]);
      setAllCitas((allAppointmentsResponse.data?.content ?? []) as AppointmentRow[]);
      setTotalCitas(allAppointmentsResponse.data?.totalElements ?? allAppointmentsResponse.data?.content?.length ?? 0);
      setTotalPages(response.data?.totalPages ?? 1);
      setPage(nextPage);
    } catch (err) {
      setError('No se pudo cargar la agenda global.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAgenda(0);
  }, []);

  const openCancellationModal = (appointment: AppointmentRow) => {
    setAppointmentToCancel(appointment);
    setCancellationReason('');
    setError('');
  };

  const closeCancellationModal = () => {
    if (cancellingId !== null) return;
    setAppointmentToCancel(null);
    setCancellationReason('');
  };

  const cancelAppointment = async () => {
    if (!appointmentToCancel) return;

    const trimmedReason = cancellationReason.trim();
    if (!trimmedReason) {
      setError('Debe indicar un motivo para cancelar la cita.');
      return;
    }

    try {
      setCancellingId(appointmentToCancel.id);
      setError('');
      await api.patch(`/api/appointments/${appointmentToCancel.id}/cancel`, { reason: trimmedReason });
      setAppointmentToCancel(null);
      setCancellationReason('');
      await fetchAgenda(page);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'No se pudo cancelar la cita.');
    } finally {
      setCancellingId(null);
    }
  };

  const confirmedCount = allCitas.filter((cita) => cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED').length;
  const pendingCount = allCitas.filter((cita) => cita.status === 'PENDING_CONFIRMATION').length;
  const totalCount = totalCitas;
  const canCancelAppointments = user?.role === 'RECEPTIONIST';

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
          <div className="mb-8 grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <Calendar className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Total</span>
              </div>
<h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{totalCount}</h3>
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
              <table className="w-full min-w-0 md:min-w-[760px]">
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
                  {citasGlobal.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-10 text-center text-[#64748B]">
                        No hay citas registradas.
                      </td>
                    </tr>
                  ) : (
                    citasGlobal.map((cita) => {
                      const fecha = new Date(cita.startTime);
                      const hora = fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', hour12: false });
                      const fechaTexto = fecha.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
                      const canCancel = canCancelAppointments && (cita.status === 'PENDING_CONFIRMATION' || cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED');

                      return (
                        <tr key={cita.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm font-medium text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Fecha">
                            <span>{fechaTexto}</span>
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm font-medium text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">
                            <span>{hora}</span>
                            
                          </td>
                          
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Paciente">
                            Paciente ID: {cita.patientId}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Médico">
                            Médico ID: {cita.doctorId}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                            <div className="flex items-center gap-3">
                              <span className={`inline-flex items-center gap-2 text-sm ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'text-[#10B981]' : 'text-[#F59E0B]'}`}>
                                <span className={`h-2 w-2 rounded-full ${cita.status === 'SCHEDULED' || cita.status === 'CONFIRMED' ? 'bg-[#10B981]' : 'bg-[#F59E0B]'}`} />
                                {cita.status}
                              </span>
                              {canCancel && (
                                <button
                                  type="button"
                                  onClick={() => openCancellationModal(cita)}
                                  disabled={cancellingId === cita.id}
                                  className="inline-flex items-center gap-1 rounded-md border border-red-200 px-2 py-1 text-xs font-medium text-red-600 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                                  title="Cancelar cita"
                                >
                                  <XCircle className="h-4 w-4" />
                                  {cancellingId === cita.id ? 'Cancelando...' : 'Cancelar'}
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            <div className="flex items-center justify-between border-t border-[#E2E8F0] bg-[#F8FAFC] px-4 py-4 sm:px-6">
              <button
                type="button"
                onClick={() => fetchAgenda(page - 1)}
                disabled={page === 0 || isLoading}
                className="inline-flex items-center gap-2 rounded-md border border-[#CBD5E1] bg-white px-3 py-2 text-sm font-medium text-[#475569] disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronLeft className="h-4 w-4" />
                Anterior
              </button>
              <span className="text-sm text-[#475569]">
                Página {page + 1} de {Math.max(totalPages, 1)}
              </span>
              <button
                type="button"
                onClick={() => fetchAgenda(page + 1)}
                disabled={page + 1 >= totalPages || isLoading}
                className="inline-flex items-center gap-2 rounded-md border border-[#CBD5E1] bg-white px-3 py-2 text-sm font-medium text-[#475569] disabled:cursor-not-allowed disabled:opacity-50"
              >
                Siguiente
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        </>
      )}

      {appointmentToCancel && canCancelAppointments && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4" role="dialog" aria-modal="true" aria-labelledby="cancel-appointment-title">
          <div className="w-full max-w-lg rounded-2xl border border-[#E2E8F0] bg-white shadow-2xl">
            <div className="flex items-start justify-between border-b border-[#E2E8F0] px-6 py-5">
              <div className="flex items-start gap-3">
                <div className="rounded-full bg-red-50 p-2 text-red-600">
                  <AlertTriangle className="h-5 w-5" />
                </div>
                <div>
                  <h2 id="cancel-appointment-title" className="text-lg font-semibold text-[#1E293B]">Cancelar cita</h2>
                  <p className="mt-1 text-sm text-[#64748B]">Esta acción cambiará el estado de la cita a cancelada.</p>
                </div>
              </div>
              <button type="button" onClick={closeCancellationModal} className="rounded-lg p-2 text-[#64748B] hover:bg-[#F8FAFC]" aria-label="Cerrar modal">
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-4 px-6 py-5">
              <div className="rounded-xl bg-[#F8FAFC] p-4 text-sm text-[#475569]">
                <div className="flex justify-between gap-4"><span>Cita</span><strong>#{appointmentToCancel.id}</strong></div>
                <div className="mt-2 flex justify-between gap-4"><span>Estado actual</span><strong>{appointmentToCancel.status}</strong></div>
              </div>
              <label htmlFor="cancellation-reason" className="block text-sm font-medium text-[#475569]">
                Motivo de cancelación
                <textarea
                  id="cancellation-reason"
                  value={cancellationReason}
                  onChange={(event) => setCancellationReason(event.target.value)}
                  placeholder="Escriba el motivo..."
                  rows={4}
                  autoFocus
                  className="mt-2 w-full resize-none rounded-lg border border-[#CBD5E1] bg-white px-3 py-2.5 text-sm text-[#1E293B] outline-none transition focus:border-[#2C7A7B] focus:ring-2 focus:ring-[#8CD6D1]"
                />
              </label>
            </div>

            <div className="flex justify-end gap-3 border-t border-[#E2E8F0] px-6 py-4">
              <button type="button" onClick={closeCancellationModal} disabled={cancellingId !== null} className="rounded-lg border border-[#CBD5E1] px-4 py-2 text-sm font-medium text-[#475569] hover:bg-[#F8FAFC] disabled:opacity-50">Volver</button>
              <button type="button" onClick={cancelAppointment} disabled={cancellingId !== null || !cancellationReason.trim()} className="inline-flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50">
                <XCircle className="h-4 w-4" />
                {cancellingId !== null ? 'Cancelando...' : 'Confirmar cancelación'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
