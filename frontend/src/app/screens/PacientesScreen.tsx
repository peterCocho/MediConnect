import { Search, Loader2, ChevronLeft, ChevronRight, Plus, Pencil, Save, X } from 'lucide-react';
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

type PatientFormState = {
  identityDocument: string;
  fullName: string;
  phone: string;
  birthDate: string;
  isActive: boolean;
};

const emptyForm: PatientFormState = {
  identityDocument: '',
  fullName: '',
  phone: '',
  birthDate: '',
  isActive: true,
};

export function PacientesScreen() {
  const [pacientes, setPacientes] = useState<PatientRow[]>([]);
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [draft, setDraft] = useState<PatientFormState>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);

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

  const resetDraft = () => {
    setDraft(emptyForm);
    setEditingId(null);
  };

  const startEditing = (patient: PatientRow) => {
    setEditingId(patient.id);
    setDraft({
      identityDocument: patient.identityDocument ?? '',
      fullName: patient.fullName ?? '',
      phone: patient.phone ?? '',
      birthDate: patient.birthDate ? new Date(patient.birthDate).toISOString().slice(0, 10) : '',
      isActive: patient.active ?? patient.isActive ?? true,
    });
    setShowCreateForm(false);
    setSuccess('');
  };

  const handleCreatePatient = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await api.post('/api/patients', {
        identityDocument: draft.identityDocument.trim(),
        fullName: draft.fullName.trim(),
        phone: draft.phone.trim(),
        birthDate: draft.birthDate || null,
      });

      setSuccess('Paciente registrado correctamente.');
      setDraft(emptyForm);
      setShowCreateForm(false);
      await fetchPatients(page);
    } catch (err: any) {
      const message = err?.response?.data?.message || 'No se pudo registrar el paciente.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdatePatient = async () => {
    if (editingId === null) return;

    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await api.put(`/api/patients/${editingId}`, {
        fullName: draft.fullName.trim(),
        phone: draft.phone.trim(),
        birthDate: draft.birthDate || null,
        isActive: draft.isActive,
      });

      setSuccess('Paciente actualizado correctamente.');
      resetDraft();
      await fetchPatients(page);
    } catch (err: any) {
      const message = err?.response?.data?.message || 'No se pudo actualizar el paciente.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleActive = async (patient: PatientRow) => {
    const nextStatus = !(patient.active ?? patient.isActive ?? true);

    try {
      setError('');
      setSuccess('');
      await api.put(`/api/patients/${patient.id}`, {
        fullName: patient.fullName,
        phone: patient.phone,
        birthDate: patient.birthDate || null,
        isActive: nextStatus,
      });

      setSuccess(`Paciente ${nextStatus ? 'activado' : 'desactivado'} correctamente.`);
      await fetchPatients(page);
    } catch (err: any) {
      const message = err?.response?.data?.message || 'No se pudo cambiar el estado del paciente.';
      setError(message);
    }
  };

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
					<h1 className="mb-2 text-2xl font-bold text-[#1E293B] sm:text-3xl">
						Gestión de Pacientes
					</h1>
					<p className="text-sm text-[#64748B] sm:text-base">
						Administre los pacientes, edite sus datos y actualice su estado.
					</p>
				</div>
				<button
					type="button"
					onClick={() => {
						setShowCreateForm((current) => !current);
						if (!showCreateForm) {
							setEditingId(null);
						}
					}}
					className="flex items-center justify-center gap-2 rounded-lg bg-[#2C7A7B] px-4 py-3 font-semibold text-white transition-colors hover:bg-[#235E5F] sm:px-6">
					<Plus className="h-5 w-5" />
					<span>{showCreateForm ? "Cerrar" : "Nuevo Paciente"}</span>
				</button>
			</div>

			{showCreateForm && (
				<div className="mb-6 rounded-xl border border-[#CBD5E1] bg-white p-6 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
					<h2 className="mb-4 text-xl font-semibold text-[#1E293B]">
						Registrar paciente
					</h2>
					<form
						onSubmit={handleCreatePatient}
						className="grid gap-4 md:grid-cols-2">
						<label className="text-sm text-[#475569]">
							Documento de identidad
							<input
								required
								value={draft.identityDocument}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										identityDocument: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<label className="text-sm text-[#475569]">
							Nombre completo
							<input
								required
								value={draft.fullName}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										fullName: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<label className="text-sm text-[#475569]">
							Teléfono
							<input
								required
								value={draft.phone}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										phone: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<label className="text-sm text-[#475569]">
							Fecha de nacimiento
							<input
								type="date"
								required
								value={draft.birthDate}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										birthDate: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<div className="md:col-span-2 flex justify-end gap-3 pt-2">
							<button
								type="button"
								onClick={() => {
									setShowCreateForm(false);
									setDraft(emptyForm);
								}}
								className="rounded-lg border border-[#CBD5E1] bg-[#F1F5F9] px-4 py-2 text-sm font-medium text-[#475569]">
								Cancelar
							</button>
							<button
								type="submit"
								disabled={submitting}
								className="rounded-lg bg-[#2C7A7B] px-4 py-2 text-sm font-semibold text-white disabled:opacity-60">
								{submitting ? "Guardando..." : "Guardar paciente"}
							</button>
						</div>
					</form>
				</div>
			)}

			{editingId !== null && (
				<div className="mb-6 rounded-xl border border-[#CBD5E1] bg-white p-6 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
					<div className="mb-4 flex items-center justify-between">
						<h2 className="text-xl font-semibold text-[#1E293B]">
							Editar paciente
						</h2>
						<button
							type="button"
							onClick={resetDraft}
							className="rounded-full p-2 text-[#64748B] hover:bg-[#F1F5F9]">
							<X className="h-4 w-4" />
						</button>
					</div>

					<div className="grid gap-4 md:grid-cols-2">
						<label className="text-sm text-[#475569]">
							Documento de identidad
							<input
								value={draft.identityDocument}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										identityDocument: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
								disabled
							/>
						</label>
						<label className="text-sm text-[#475569]">
							Estado
							<select
								value={String(draft.isActive)}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										isActive: event.target.value === "true",
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2">
								<option value="true">Activo</option>
								<option value="false">Inactivo</option>
							</select>
						</label>
						<label className="text-sm text-[#475569] md:col-span-2">
							Nombre completo
							<input
								value={draft.fullName}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										fullName: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<label className="text-sm text-[#475569]">
							Teléfono
							<input
								value={draft.phone}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										phone: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<label className="text-sm text-[#475569]">
							Fecha de nacimiento
							<input
								type="date"
								value={draft.birthDate}
								onChange={(event) =>
									setDraft((current) => ({
										...current,
										birthDate: event.target.value,
									}))
								}
								className="mt-1 w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2"
							/>
						</label>
						<div className="md:col-span-2 flex justify-end gap-3 pt-2">
							<button
								type="button"
								onClick={resetDraft}
								className="rounded-lg border border-[#CBD5E1] bg-[#F1F5F9] px-4 py-2 text-sm font-medium text-[#475569]">
								Cancelar
							</button>
							<button
								type="button"
								onClick={handleUpdatePatient}
								disabled={submitting}
								className="inline-flex items-center gap-2 rounded-lg bg-[#2C7A7B] px-4 py-2 text-sm font-semibold text-white disabled:opacity-60">
								<Save className="h-4 w-4" />
								{submitting ? "Guardando..." : "Guardar cambios"}
							</button>
						</div>
					</div>
				</div>
			)}

			<div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
				<div className="border-b border-[#E2E8F0] p-4 sm:p-6">
					<div className="relative">
						<Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-[#64748B]" />
						<input
							type="text"
							value={query}
							onChange={(event) => setQuery(event.target.value)}
							placeholder="Buscar por nombre, documento o teléfono..."
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
						{success && (
							<div className="border-b border-[#E2E8F0] bg-emerald-50 px-6 py-3 text-sm text-emerald-700">
								{success}
							</div>
						)}
						<div className="overflow-x-auto">
							<table className="w-full min-w-0">
								<thead className="hidden md:table-header-group">
									<tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
										id
										</th>
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
											Identificación
										</th>
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
											Nombre Completo
										</th>
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
											Teléfono
										</th>
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
											Fecha Nacimiento
										</th>
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
											Estado
										</th>
										<th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">
											Acciones
										</th>
									</tr>
								</thead>
								<tbody>
									{filteredPatients.length === 0 ? (
										<tr>
											<td
												colSpan={6}
												className="px-6 py-10 text-center text-[#64748B]">
												No hay pacientes para mostrar.
											</td>
										</tr>
									) : (
										filteredPatients.map((paciente) => {
											const isActive =
												paciente.active ?? paciente.isActive ?? false;

											return (
												<tr
													key={paciente.id}
                          className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                          <td
														className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Id">
														{paciente.id}
													</td>
													<td
														className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Identificación">
														{paciente.identityDocument}
													</td>
													<td
														className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Nombre Completo">
														{paciente.fullName}
													</td>
													<td
														className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Teléfono">
														{paciente.phone}
													</td>
													<td
														className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Fecha Nacimiento">
														{paciente.birthDate
															? new Date(paciente.birthDate).toLocaleDateString(
																	"es-ES",
																)
															: "—"}
													</td>
													<td
														className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Estado">
														<span
															className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${isActive ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-600"}`}>
															{isActive ? "Activo" : "Inactivo"}
														</span>
													</td>
													<td
														className="flex items-center justify-between gap-2 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden"
														data-label="Acciones">
														<button
															type="button"
															onClick={() => startEditing(paciente)}
															className="inline-flex items-center gap-1 rounded-md border border-[#CBD5E1] bg-white px-2.5 py-1.5 text-xs font-medium text-[#475569] hover:bg-[#F8FAFC]">
															<Pencil className="h-3.5 w-3.5" />
															Editar
														</button>
														<button
															type="button"
															onClick={() => handleToggleActive(paciente)}
															className="rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-2.5 py-1.5 text-xs font-medium text-[#475569] hover:bg-[#E2E8F0]">
															{isActive ? "Desactivar" : "Activar"}
														</button>
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
								className="inline-flex items-center gap-2 rounded-md border border-[#CBD5E1] bg-white px-3 py-2 text-sm font-medium text-[#475569] disabled:cursor-not-allowed disabled:opacity-50">
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
								className="inline-flex items-center gap-2 rounded-md border border-[#CBD5E1] bg-white px-3 py-2 text-sm font-medium text-[#475569] disabled:cursor-not-allowed disabled:opacity-50">
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