import { Search, Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import api from '../../service/api';

type PatientRow = {
  id: number;
  identityDocument: string;
  fullName: string;
  phone: string;
  birthDate: string | null;
  medicalRecordId?: number | null;
};

export function MisPacientesScreen({ onVerHistorial }: { onVerHistorial?: (medicalRecordId: number) => void }) {
  const [pacientes, setPacientes] = useState<PatientRow[]>([]);
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDoctorPatients = async () => {
      try {
        setIsLoading(true);
        setError('');

        const [patientResponse, consultationResponse] = await Promise.all([
          api.get('/api/patients', { params: { page: 0, size: 100, sortBy: 'id' } }),
          api.get('/api/consultations', { params: { page: 0, size: 100, sort: 'consultationDate,desc' } }),
        ]);

        const patientRows = patientResponse.data?.content ?? [];
        const medicalRecordIds = new Set(
          (consultationResponse.data?.content ?? [])
            .map((consultation: any) => consultation.medicalRecordId)
            .filter((id: number | null) => id !== null && id !== undefined)
        );

        const assigned = patientRows.filter((patient: PatientRow) => {
          const recordId = patient.medicalRecordId ?? null;
          return recordId !== null && medicalRecordIds.has(recordId);
        });

        setPacientes(assigned);
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
    if (!normalized) return pacientes;

    return pacientes.filter((paciente) => {
      const searchText = `${paciente.fullName} ${paciente.identityDocument} ${paciente.phone}`.toLowerCase();
      return searchText.includes(normalized);
    });
  }, [pacientes, query]);

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
                <thead>
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
                      <tr key={paciente.id} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                        <td className="px-6 py-4 text-sm font-medium text-[#1E293B]">{paciente.identityDocument}</td>
                        <td className="px-6 py-4 text-sm text-[#1E293B]">{paciente.fullName}</td>
                        <td className="px-6 py-4 text-sm text-[#1E293B]">{paciente.phone}</td>
                        <td className="px-6 py-4 text-sm text-[#1E293B]">{paciente.birthDate ? new Date(paciente.birthDate).toLocaleDateString('es-ES') : '—'}</td>
                        <td className="px-6 py-4 text-sm text-[#64748B]">#{paciente.medicalRecordId ?? '—'}</td>
                        <td className="px-6 py-4">
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