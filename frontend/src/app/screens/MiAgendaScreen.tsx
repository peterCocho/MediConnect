import { Calendar, Clock, CheckCircle, Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import api from '../../service/api';

type ConsultationRow = {
  id: number;
  consultationDate: string;
  status: string;
  medicalRecordId: number | null;
  doctorId: number | null;
};

const getDateKey = (value: string | Date) => {
  const date = value instanceof Date ? value : new Date(value);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

export function MiAgendaScreen({ onIniciarConsulta }: { onIniciarConsulta?: (consultationId: number) => void }) {
  const [consultas, setConsultas] = useState<ConsultationRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadAgenda = async () => {
      try {
        setIsLoading(true);
        setError('');
        const response = await api.get('/api/consultations', {
          params: { page: 0, size: 20, sort: 'consultationDate,desc' },
        });
        setConsultas(response.data?.content ?? []);
      } catch (err) {
        setError('No se pudo cargar la agenda del doctor.');
      } finally {
        setIsLoading(false);
      }
    };

    loadAgenda();
  }, []);

  const today = getDateKey(new Date());
  const agenda = useMemo(() => (consultas ?? []).filter((consulta) => consulta.consultationDate && getDateKey(consulta.consultationDate) === today), [consultas, today]);
  const totalDay = agenda.filter((consulta) => consulta.status !== 'CANCELED').length;
  const waitingRoom = agenda.filter((consulta) => consulta.status === 'CONFIRMED').length;
  const attendedToday = agenda.filter((consulta) => consulta.status === 'COMPLETED').length;

  const formatHour = (value: string) => {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' : date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', hour12: false });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#F4F7F9] p-8">
        <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Cargando agenda...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Mi Agenda</h1>
        <p className="text-[#64748B]">Consultas programadas del médico autenticado</p>
      </div>

      <div className="mb-8 grid gap-6 sm:grid-cols-3">
        <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
              <Calendar className="h-6 w-6 text-[#2C7A7B]" />
            </div>
            <span className="text-sm text-[#64748B]">Hoy</span>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{totalDay}</h3>
          <p className="text-sm text-[#64748B]">Total del día</p>
        </div>

        <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#10B981] bg-opacity-20 p-3">
              <Clock className="h-6 w-6 text-[#10B981]" />
            </div>
            <span className="text-sm text-[#64748B]">Estado</span>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{waitingRoom}</h3>
          <p className="text-sm text-[#64748B]">En sala de espera</p>
        </div>

        <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
              <CheckCircle className="h-6 w-6 text-[#10B981]" />
            </div>
            <span className="text-sm text-[#64748B]">Hoy</span>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{attendedToday}</h3>
          <p className="text-sm text-[#64748B]">Atendidos hoy</p>
        </div>
      </div>

      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
          <h2 className="text-xl font-semibold text-[#1E293B]">Listado de pacientes agendados</h2>
        </div>

        {error ? (
          <div className="px-6 py-8 text-center text-red-600">{error}</div>
        ) : agenda.length === 0 ? (
          <div className="px-6 py-10 text-center text-[#64748B]">No hay consultas programadas para este médico.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="hidden md:table-header-group">
                <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Hora</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Expediente</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Fecha</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Estado</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Acción</th>
                </tr>
              </thead>
              <tbody>
                {agenda.map((consulta) => (
                  <tr key={consulta.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                    <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm font-semibold text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">{formatHour(consulta.consultationDate)}</td>
                    <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Expediente">#{consulta.medicalRecordId ?? '—'}</td>
                    <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#64748B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Fecha">{new Date(consulta.consultationDate).toLocaleDateString('es-ES')}</td>
                    <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                      <span className="inline-flex items-center gap-2 text-sm text-[#10B981]">
                        <span className="h-2 w-2 rounded-full bg-[#10B981]" />
                        {consulta.status}
                      </span>
                    </td>
                    <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Acción">
                      <button
                        type="button"
                        onClick={() => onIniciarConsulta?.(consulta.id)}
                        className="rounded-lg bg-[#2C7A7B] px-6 py-2 text-sm font-semibold text-white transition-colors hover:bg-[#235E5F]"
                      >
                        Iniciar Consulta
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}