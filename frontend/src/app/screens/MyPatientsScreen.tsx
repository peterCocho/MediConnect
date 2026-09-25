import { Search, Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import api from '../../service/api';

interface ConsultationDto {
  medicalRecordId?: number | null;
  patientId?: number | null;
  identityDocument: string;
  fullName: string;
  phone: string;
  birthDate: string | null;
}

type PatientRow = {
  id: number;
  patientId: number;
  identityDocument: string;
  fullName: string;
  phone: string;
  birthDate: string | null;
  medicalRecordId?: number | null;
};

/**
 * Screen listing the patients attended by the authenticated doctor.
 *
 * Fetches the doctor's consultations via `GET /api/consultations/patients`,
 * de-duplicates them by medical record and renders a searchable table
 * (identity document, full name, phone, birth date, medical record) whose
 * "Ver Historial" button reports the selected record through the
 * `onVerHistorial` callback.
 *
 * @param props - Component props.
 * @param props.onVerHistorial - Optional callback invoked with the
 * `medicalRecordId` when the user clicks "Ver Historial" on a row; the button
 * is disabled when the row has no associated medical record.
 *
 * @returns The patients directory with a search input, a table of attended
 * patients, a loading spinner, or an error message when the list cannot be
 * loaded.
 */
export function MyPatientsScreen({ onVerHistorial }: { onVerHistorial?: (medicalRecordId: number) => void }) {
  const [patients, setPatients] = useState<PatientRow[]>([]);
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDoctorPatients = async () => {
      try {
        setIsLoading(true);
        setError('');

        const consultationResponse = await api.get('/api/consultations/patients', {
          params: { page: 0, size: 100, sort: 'consultationDate,desc' },
        });

        const uniquePatients = new Map<number, PatientRow>();

        // 2. Replace 'any' with 'ConsultationDto'
        (consultationResponse.data?.content ?? []).forEach((consultation: ConsultationDto) => {
          // Guard clause against potentially null objects from the API array
          if (!consultation) return;

          const recordId = consultation.medicalRecordId;
          const patientId = consultation.patientId;
          
          if (recordId == null || patientId == null) return;

          const compiled = {
            id: patientId,
            patientId,
            identityDocument: consultation.identityDocument,
            fullName: consultation.fullName,
            phone: consultation.phone,
            birthDate: consultation.birthDate,
            medicalRecordId: recordId,
          } satisfies PatientRow;

          uniquePatients.set(recordId, compiled);
        });
        
        setPatients(Array.from(uniquePatients.values()));
      } catch (err) {
        setError('No se pudieron cargar los pacientes atendidos por este doctor.');
      } finally {
        setIsLoading(false);
      }
    };

    loadDoctorPatients();
  }, []);

  const filteredPatients = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return patients;

    return patients.filter((paciente) => {
      const searchText = `${paciente.fullName} ${paciente.identityDocument} ${paciente.phone}`.toLowerCase();
      return searchText.includes(normalized);
    });
  }, [patients, query]);

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Mis Pacientes</h1>
        <p className="text-[#64748B]">Pacientes asociados a las consultas atendidas por el médico autenticado</p>
      </div>

      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="flex items-center justify-between border-b border-[#E2E8F0] p-6">
          <div className="relative w-full max-w-md">
            <Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-[#64748B]" />
            <input
              type="text"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Buscar por nombre o documento..."
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] py-3 pl-10 pr-4 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
            />
          </div>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
            <Loader2 className="h-5 w-5 animate-spin" />
            <span>Cargando pacientes...</span>
          </div>
        ) : error ? (
          <div className="px-6 py-8 text-center text-red-600">{error}</div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="hidden md:table-header-group">
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Identificación</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Nombre Completo</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Teléfono</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Fecha Nacimiento</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Expediente</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPatients.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-6 py-10 text-center text-[#64748B]">
                        No hay pacientes para mostrar.
                      </td>
                    </tr>
                  ) : (
                    filteredPatients.map((paciente) => (
                      <tr key={paciente.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm font-medium text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Identificación">{paciente.identityDocument}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Nombre Completo">{paciente.fullName}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Teléfono">{paciente.phone}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Fecha Nacimiento">{paciente.birthDate ? new Date(paciente.birthDate).toLocaleDateString('es-ES') : '—'}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#64748B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Expediente">#{paciente.medicalRecordId ?? '—'}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Acción">
                          <button
                            type="button"
                            onClick={() => paciente.medicalRecordId && onVerHistorial?.(paciente.medicalRecordId)}
                            disabled={!paciente.medicalRecordId}
                            className="rounded-lg bg-[#2C7A7B] px-6 py-2 text-sm font-semibold text-white transition-colors hover:bg-[#235E5F] disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            Ver Historial
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </div>
  );
}