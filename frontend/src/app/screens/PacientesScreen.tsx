import { Search, Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import api from '../../service/api';

type PatientRow = {
  id: number;
  identityDocument: string;
  fullName: string;
  phone: string;
  birthDate: string | null;
  isActive?: boolean;
  active?: boolean;
};

export function PacientesScreen() {
  const [pacientes, setPacientes] = useState<PatientRow[]>([]);
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchPatients = async (nextPage = 0) => {
    try {
      setIsLoading(true);
      setError('');
      const response = await api.get('/api/patients', {
        params: { page: nextPage, size: 10, sortBy: 'id' },
      });

      const data = response.data?.content ?? response.data ?? [];
      setPacientes(Array.isArray(data) ? data : []);
      setTotalPages(response.data?.totalPages ?? 1);
      setPage(nextPage);
    } catch (err) {
      setError('No se pudieron cargar los pacientes. Intente nuevamente.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPatients(0);
  }, []);

  const filteredPatients = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return pacientes;

    return pacientes.filter((paciente) => {
      const searchable = [
        paciente.fullName,
        paciente.identityDocument,
        paciente.phone,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      return searchable.includes(normalized);
    });
  }, [pacientes, query]);

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-6 flex flex-col gap-3 sm:mb-8 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="mb-2 text-2xl font-bold text-[#1E293B] sm:text-3xl">Gestión de Pacientes</h1>
          <p className="text-sm text-[#64748B] sm:text-base">Administre los registros de pacientes del sistema</p>
        </div>
      </div>

      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="border-b border-[#E2E8F0] p-4 sm:p-6">
          <div className="relative">
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
              <table className="min-w-[760px] w-full">
                <thead>
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Identificación</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Nombre Completo</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Teléfono</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Fecha Nacimiento</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPatients.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-10 text-center text-[#64748B]">
                        No hay pacientes para mostrar.
                      </td>
                    </tr>
                  ) : (
                    filteredPatients.map((paciente) => {
                      const isActive = paciente.isActive ?? paciente.active ?? true;
                      return (
                        <tr key={paciente.id} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                          <td className="px-4 py-4 text-sm text-[#1E293B] sm:px-6">{paciente.identityDocument}</td>
                          <td className="px-4 py-4 text-sm text-[#1E293B] sm:px-6">{paciente.fullName}</td>
                          <td className="px-4 py-4 text-sm text-[#1E293B] sm:px-6">{paciente.phone}</td>
                          <td className="px-4 py-4 text-sm text-[#1E293B] sm:px-6">
                            {paciente.birthDate ? new Date(paciente.birthDate).toLocaleDateString('es-ES') : '—'}
                          </td>
                          <td className="px-4 py-4 sm:px-6">
                            <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${isActive ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                              {isActive ? 'Activo' : 'Inactivo'}
                            </span>
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
                onClick={() => fetchPatients(page - 1)}
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
                onClick={() => fetchPatients(page + 1)}
                disabled={page + 1 >= totalPages || isLoading}
                className="inline-flex items-center gap-2 rounded-md border border-[#CBD5E1] bg-white px-3 py-2 text-sm font-medium text-[#475569] disabled:cursor-not-allowed disabled:opacity-50"
              >
                Siguiente
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}