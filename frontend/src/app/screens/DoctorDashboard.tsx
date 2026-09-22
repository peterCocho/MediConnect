import {
  Calendar as CalendarIcon,
  Clock,
  ClipboardList,
  Loader2,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';
import { translateStatus } from '../utils/statusLabels';

type DoctorConsultation = {
  id: number;
  appointmentId?: number | null;
  consultationDate?: string | null;
  status?: string | null;
  medicalRecordId?: number | null;
  icd10Code?: string | null;
  reasonForVisit?: string | null;
};


const getDateKey = (value: string | Date) => {
  const date = value instanceof Date ? value : new Date(value);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

export function DoctorDashboard() {
  const [consultations, setConsultations] = useState<DoctorConsultation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDoctorDashboard = async () => {
      try {
        setIsLoading(true);
        setError('');

        const response = await api.get('/api/consultations', {
          params: { page: 0, size: 1000, sort: 'consultationDate,desc' },
        });

        setConsultations((response.data?.content ?? []) as DoctorConsultation[]);
      } catch (err) {
        setError('No se pudo cargar la agenda médica.');
      } finally {
        setIsLoading(false);
      }
    };

    loadDoctorDashboard();
  }, []);

  const today = getDateKey(new Date());
  const todayConsultations = consultations.filter((consultation) => consultation.consultationDate && getDateKey(consultation.consultationDate) === today);
  const nextAppointment = todayConsultations
    .filter((consultation) => consultation.status === 'SCHEDULED' || consultation.status === 'CONFIRMED')
    .sort((first, second) => new Date(first.consultationDate ?? 0).getTime() - new Date(second.consultationDate ?? 0).getTime())[0];
  const last30Days = new Date();
  last30Days.setHours(0, 0, 0, 0);
  last30Days.setDate(last30Days.getDate() - 29);
  const patientsThisMonth = new Set(
    consultations
      .filter((consultation) => (consultation.status === 'COMPLETED' || consultation.status === 'FINALIZED') && consultation.consultationDate && new Date(consultation.consultationDate) >= last30Days)
      .map((consultation) => consultation.medicalRecordId)
      .filter((medicalRecordId): medicalRecordId is number => medicalRecordId !== null && medicalRecordId !== undefined),
  ).size;

const statusColors: Record<string, { text: string; bg: string }> = {
    COMPLETED: { text: 'text-[#10B981]', bg: 'bg-[#10B981]' },
    FINALIZED: { text: 'text-[#10B981]', bg: 'bg-[#10B981]' },
    SCHEDULED: { text: 'text-[#3B82F6]', bg: 'bg-[#3B82F6]' },
    CONFIRMED: { text: 'text-[#3B82F6]', bg: 'bg-[#3B82F6]' },
    PENDING_CONFIRMATION: { text: 'text-[#F59E0B]', bg: 'bg-[#F59E0B]' },
    CANCELED: { text: 'text-[#EF4444]', bg: 'bg-[#EF4444]' },
  };
  const defaultColor = { text: 'text-[#64748B]', bg: 'bg-[#64748B]' };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Panel del Médico</h1>
        <p className="text-[#64748B]">Resumen de su agenda clínica y consultas recientes</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Cargando agenda médica...</span>
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
                <span className="text-sm text-[#64748B]">Hoy</span>
              </div>
              <h3 className="mb-1 text-xl font-bold text-[#1E293B]">
                {nextAppointment?.consultationDate ? new Date(nextAppointment.consultationDate).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', hour12: false }) : 'Sin citas'}
              </h3>
              <p className="text-sm text-[#64748B]">Próxima cita · Expediente #{nextAppointment?.medicalRecordId ?? '—'}</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <Clock className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Hoy</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{todayConsultations.length}</h3>
              <p className="text-sm text-[#64748B]">Consultas de hoy</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <ClipboardList className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Últimos 30 días</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{patientsThisMonth}</h3>
              <p className="text-sm text-[#64748B]">Pacientes atendidos</p>
            </div>
          </div>

          <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
            <div className="rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
              <h2 className="text-xl font-semibold text-[#1E293B]">Consultas recientes</h2>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-0">
                <thead className="hidden md:table-header-group">
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Fecha</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Hora</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Expediente</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Diagnóstico</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {consultations.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-10 text-center text-[#64748B]">No hay consultas registradas para este médico.</td>
                    </tr>
                  ) : (
                    consultations.map((consultation) => {
                      const currentStatus = consultation.status ?? '';
                      const currentStyle = statusColors[currentStatus] || defaultColor;

                      return (
                        <tr key={consultation.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Fecha">
                            {consultation.consultationDate
                              ? new Date(consultation.consultationDate).toLocaleDateString('es-ES', { 
                                  day: '2-digit', 
                                  month: '2-digit', 
                                  year: 'numeric',
                                  timeZone: 'America/Bogota' 
                                })
                              : 'Sin fecha'}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Hora">
                            {consultation.consultationDate
                              ? new Date(consultation.consultationDate).toLocaleTimeString('es-ES', { 
                                  hour: '2-digit', 
                                  minute: '2-digit', 
                                  hour12: false,
                                  timeZone: 'America/Bogota' 
                                })
                              : 'Sin hora'}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Expediente">
                            #{consultation.medicalRecordId ?? '—'}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Diagnóstico">
                            {consultation.icd10Code ?? 'Sin diagnóstico'}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                            <span className={`inline-flex items-center gap-2 text-sm ${currentStyle.text}`}>
                              <span className={`h-2 w-2 rounded-full ${currentStyle.bg}`} />
                              {translateStatus(consultation.status)}
                            </span>
                          </td>
                        </tr>
                      );
                    })
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
