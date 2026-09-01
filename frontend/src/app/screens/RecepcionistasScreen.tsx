import { Loader2, Plus, UserCheck, ChevronLeft, ChevronRight, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';

type Receptionist = {
  id: number;
  username: string;
  identityDocument: string;
  fullName: string;
  phone: string;
  isActive?: boolean;
  active?: boolean;
};

type ReceptionistForm = {
  username: string;
  password: string;
  fullName: string;
  identityDocument: string;
  phone: string;
};

const emptyReceptionistForm: ReceptionistForm = {
  username: '',
  password: '',
  fullName: '',
  identityDocument: '',
  phone: '',
};

export function RecepcionistasScreen() {
  const [recepcionistas, setRecepcionistas] = useState<Receptionist[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<ReceptionistForm>(emptyReceptionistForm);
  const [submitting, setSubmitting] = useState(false);
  const [statusUpdatingId, setStatusUpdatingId] = useState<number | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const loadReceptionists = async (nextPage = 0) => {
    try {
      setIsLoading(true);
      setError('');
      const response = await api.get('/api/receptionists', {
        params: { page: nextPage, size: 10, sortBy: 'id' },
      });
      const data = response.data?.content ?? response.data ?? [];
      setRecepcionistas(Array.isArray(data) ? data : []);
      setTotalPages(response.data?.totalPages ?? 1);
      setPage(nextPage);
    } catch (err) {
      setError('No se pudo cargar la información de recepcionistas.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadReceptionists(0);
  }, []);

  const handleChange = (field: keyof ReceptionistForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await api.post('/api/receptionists', form);
      setSuccess('Recepcionista creado correctamente.');
      setForm(emptyReceptionistForm);
      setShowForm(false);
      await loadReceptionists(page);
    } catch (err: any) {
      const message = err?.response?.data?.message || 'No se pudo crear el recepcionista.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (recepcionista: Receptionist) => {
    const nextStatus = !(recepcionista.active ?? recepcionista.isActive ?? false);

    try {
      setStatusUpdatingId(recepcionista.id);
      setError('');
      await api.put(`/api/receptionists/${recepcionista.id}`, {
        fullName: recepcionista.fullName,
        phone: recepcionista.phone,
        identityDocument: recepcionista.identityDocument,
        isActive: nextStatus,
      });
      setSuccess(nextStatus ? 'El recepcionista fue activado correctamente.' : 'El recepcionista fue inactivado correctamente.');
      await loadReceptionists(page);
    } catch (err: any) {
      const message = err?.response?.data?.message || 'No se pudo actualizar el estado del recepcionista.';
      setError(message);
    } finally {
      setStatusUpdatingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-6 flex flex-col gap-3 sm:mb-8 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="mb-2 text-2xl font-bold text-[#1E293B] sm:text-3xl">Recepcionistas</h1>
          <p className="text-sm text-[#64748B] sm:text-base">Administración del personal de recepción</p>
        </div>
        <button
          type="button"
          onClick={() => setShowForm((current) => !current)}
          className="flex items-center justify-center gap-2 rounded-lg bg-[#2C7A7B] px-4 py-3 font-semibold text-white transition-colors hover:bg-[#235E5F] sm:px-6"
        >
          <Plus className="h-5 w-5" />
          <span>{showForm ? 'Cerrar' : 'Nuevo Recepcionista'}</span>
        </button>
      </div>

      {showForm && (
        <div className="mb-6 rounded-xl border border-[#CBD5E1] bg-white p-6 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-semibold text-[#1E293B]">Crear recepcionista</h2>
            <button type="button" onClick={() => setShowForm(false)} className="rounded-full p-2 text-[#64748B] hover:bg-[#F1F5F9]">
              <X className="h-4 w-4" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-2">
            <label className="text-sm text-[#475569]">
              Usuario
              <input required value={form.username} onChange={(event) => handleChange('username', event.target.value)} className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2" />
            </label>
            <label className="text-sm text-[#475569]">
              Contraseña
              <input required type="password" value={form.password} onChange={(event) => handleChange('password', event.target.value)} className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2" />
            </label>
            <label className="text-sm text-[#475569] md:col-span-2">
              Nombre completo
              <input required value={form.fullName} onChange={(event) => handleChange('fullName', event.target.value)} className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2" />
            </label>
            <label className="text-sm text-[#475569]">
              Documento de identidad
              <input required value={form.identityDocument} onChange={(event) => handleChange('identityDocument', event.target.value)} className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2" />
            </label>
            <label className="text-sm text-[#475569]">
              Teléfono
              <input required value={form.phone} onChange={(event) => handleChange('phone', event.target.value)} className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2" />
            </label>

            <div className="md:col-span-2 flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowForm(false)} className="rounded-lg border border-[#CBD5E1] bg-[#F1F5F9] px-4 py-2 text-sm font-medium text-[#475569]">
                Cancelar
              </button>
              <button type="submit" disabled={submitting} className="rounded-lg bg-[#2C7A7B] px-4 py-2 text-sm font-semibold text-white disabled:opacity-60">
                {submitting ? 'Creando...' : 'Crear recepcionista'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        {isLoading ? (
          <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
            <Loader2 className="h-5 w-5 animate-spin" />
            <span>Cargando recepcionistas...</span>
          </div>
        ) : error ? (
          <div className="px-6 py-8 text-center text-red-600">{error}</div>
        ) : (
          <>
            {success && <div className="border-b border-[#E2E8F0] bg-emerald-50 px-6 py-3 text-sm text-emerald-700">{success}</div>}
            <div className="overflow-x-auto">
              <table className="w-full min-w-0">
                <thead className="hidden md:table-header-group">
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Nombre</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Documento</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Usuario</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Teléfono</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {recepcionistas.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-10 text-center text-[#64748B]">No hay recepcionistas registrados.</td>
                    </tr>
                  ) : (
                    recepcionistas.map((recepcionista) => {
                      const isActive = recepcionista.active ?? recepcionista.isActive ?? false;

                      return (
                        <tr key={recepcionista.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Nombre">
                            {recepcionista.fullName}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Documento">
                            {recepcionista.identityDocument}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Usuario">
                            {recepcionista.username}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Teléfono">
                            {recepcionista.phone}
                          </td>
                          <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                            <div className="flex flex-col items-end gap-2 md:flex-row md:items-center md:justify-start">
                              <span className={`inline-flex items-center gap-2 rounded-full px-2.5 py-1 text-xs font-medium ${isActive ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                                <UserCheck className="h-3.5 w-3.5" />
                                {isActive ? 'Activo' : 'Inactivo'}
                              </span>
                              <button
                                type="button"
                                onClick={() => handleToggleStatus(recepcionista)}
                                disabled={statusUpdatingId === recepcionista.id}
                                className={`rounded-md px-2.5 py-1.5 text-xs font-semibold transition-colors ${
                                  isActive
                                    ? 'border border-red-200 bg-red-50 text-red-700 hover:bg-red-100'
                                    : 'border border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                                } disabled:cursor-not-allowed disabled:opacity-60`}
                              >
                                {statusUpdatingId === recepcionista.id ? 'Guardando...' : isActive ? 'Inactivar' : 'Activar'}
                              </button>
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
                onClick={() => loadReceptionists(page - 1)}
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
                onClick={() => loadReceptionists(page + 1)}
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
