import { Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router';
import api from '../../service/api';

type ConsultationForm = {
  systolicPressure: string;
  diastolicPressure: string;
  heartRate: string;
  weight: string;
  icd10Code: string;
  reasonForVisit: string;
  clinicalNotes: string;
  managementPlan: string;
};

// Se define el tipo para manejar las plantillas que llegan del backend
type ClinicalTemplate = {
  id: number;
  name: string;
  description: string;
  templateContent: string;
};

const emptyForm: ConsultationForm = {
  systolicPressure: '',
  diastolicPressure: '',
  heartRate: '',
  weight: '',
  icd10Code: '',
  reasonForVisit: '',
  clinicalNotes: '',
  managementPlan: '',
};

export function ConsultaScreen() {
  const [searchParams] = useSearchParams();
  const consultationId = searchParams.get('consultationId');
const [form, setForm] = useState<ConsultationForm>(emptyForm);
  const [templates, setTemplates] = useState<ClinicalTemplate[]>([]);
  const [medicalRecordId, setMedicalRecordId] = useState<string | null>(null); // New state for medical record
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const title = useMemo(() => (consultationId ? `Consulta #${consultationId}` : 'Consulta Médica'), [consultationId]);

  // Carga inicial de la consulta
  useEffect(() => {
    const loadConsultation = async () => {
      if (!consultationId) {
        setIsLoading(false);
        setError('No se ha seleccionado una consulta para iniciar.');
        return;
      }

      try {
        setIsLoading(true);
        setError('');
        const response = await api.get(`/api/consultations/${consultationId}`);
        const data = response.data || {};

        setMedicalRecordId(data.medicalRecordId?.toString() ?? null);

        setForm({
          systolicPressure: data.systolicPressure ?? '',
          diastolicPressure: data.diastolicPressure ?? '',
          heartRate: data.heartRate ?? '',
          weight: data.weight ?? '',
          icd10Code: data.icd10Code ?? '',
          reasonForVisit: data.reasonForVisit ?? '',
          clinicalNotes: data.clinicalNotes ?? '',
          managementPlan: data.managementPlan ?? '',
        });
      } catch (err) {
        setError('No se pudo cargar la consulta seleccionada.');
      } finally {
        setIsLoading(false);
      }
    };

    loadConsultation();
  }, [consultationId]);

  // Carga independiente de las plantillas activas
  useEffect(() => {
    const loadTemplates = async () => {
      try {
        const response = await api.get('/api/templates/active');
        setTemplates(response.data || []);
      } catch (err) {
        console.error('Error al cargar las plantillas clínicas:', err);
      }
    };

    loadTemplates();
  }, []);

  const handleChange = (field: keyof ConsultationForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  // Función para inyectar la plantilla seleccionada en las notas clínicas
  const handleTemplateSelect = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const templateId = Number(event.target.value);
    if (!templateId) return;

    const selectedTemplate = templates.find((t) => t.id === templateId);
    if (selectedTemplate && selectedTemplate.templateContent) {
      setForm((current) => {
        const newNotes = current.clinicalNotes.trim()
          ? `${current.clinicalNotes}\n\n${selectedTemplate.templateContent}`
          : selectedTemplate.templateContent;
        return { ...current, clinicalNotes: newNotes };
      });
    }

    // Reinicia el selector para permitir inyectar la misma plantilla otra vez si se requiere
    event.target.value = '';
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!consultationId) {
      setError('Debe seleccionarse una consulta antes de guardar.');
      return;
    }

    try {
      setIsSaving(true);
      setError('');
      setSuccess('');

      await api.put(`/api/consultations/${consultationId}/execute`, {
        systolicPressure: Number(form.systolicPressure),
        diastolicPressure: Number(form.diastolicPressure),
        heartRate: Number(form.heartRate),
        weight: Number(form.weight),
        icd10Code: form.icd10Code,
        reasonForVisit: form.reasonForVisit,
        clinicalNotes: form.clinicalNotes,
        managementPlan: form.managementPlan,
      });

      setSuccess('Consulta registrada correctamente.');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'No se pudo guardar la consulta. Verifique los datos ingresados.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
		<div className="min-h-screen bg-[#F4F7F9] p-8">
			<div className="mb-8">
				<h1 className="mb-2 text-3xl font-bold text-[#1E293B]">{title}</h1>
				<p className="text-[#64748B]">
					Registro de atención médica del paciente
				</p>
			</div>

			<div className="rounded-xl border border-[#CBD5E1] bg-white p-8 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
				{isLoading ? (
					<div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
						<Loader2 className="h-5 w-5 animate-spin" />
						<span>Cargando consulta...</span>
					</div>
				) : (
					<form onSubmit={handleSubmit} className="space-y-8">
						{error && (
							<div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
								{error}
							</div>
						)}
						{success && (
							<div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
								{success}
							</div>
						)}

						<div className="rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] p-4">
							<h3 className="mb-2 font-semibold text-[#1E293B]">
								Expediente clínico
							</h3>
							<p className="text-sm text-[#64748B]">
								Consulta médica asociada a expediente #
								{medicalRecordId ?? "N/A"}
							</p>
						</div>

						<div>
							<h3 className="mb-4 border-b-2 border-[#8CD6D1] pb-2 text-lg font-semibold text-[#1E293B]">
								Signos Vitales
							</h3>
							<div className="grid grid-cols-1 gap-4 md:grid-cols-4">
								<div>
									<label className="mb-2 block text-sm font-medium text-[#64748B]">
										Presión Sistólica
									</label>
									<input
										value={form.systolicPressure}
										onChange={(event) =>
											handleChange("systolicPressure", event.target.value)
										}
										type="number"
										placeholder="120"
										className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
									/>
									<span className="mt-1 block text-xs text-[#64748B]">
										mmHg
									</span>
								</div>
								<div>
									<label className="mb-2 block text-sm font-medium text-[#64748B]">
										Presión Diastólica
									</label>
									<input
										value={form.diastolicPressure}
										onChange={(event) =>
											handleChange("diastolicPressure", event.target.value)
										}
										type="number"
										placeholder="80"
										className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
									/>
									<span className="mt-1 block text-xs text-[#64748B]">
										mmHg
									</span>
								</div>
								<div>
									<label className="mb-2 block text-sm font-medium text-[#64748B]">
										Frecuencia Cardíaca
									</label>
									<input
										value={form.heartRate}
										onChange={(event) =>
											handleChange("heartRate", event.target.value)
										}
										type="number"
										placeholder="75"
										className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
									/>
									<span className="mt-1 block text-xs text-[#64748B]">lpm</span>
								</div>
								<div>
									<label className="mb-2 block text-sm font-medium text-[#64748B]">
										Peso
									</label>
									<input
										value={form.weight}
										onChange={(event) =>
											handleChange("weight", event.target.value)
										}
										type="number"
										step="0.1"
										placeholder="70.5"
										className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
									/>
									<span className="mt-1 block text-xs text-[#64748B]">kg</span>
								</div>
							</div>
						</div>

						<div>
							<label className="mb-2 block text-sm font-medium text-[#64748B]">
								Diagnóstico CIE-10
							</label>
							<input
								value={form.icd10Code}
								onChange={(event) =>
									handleChange("icd10Code", event.target.value)
								}
								type="text"
								placeholder="I10"
								className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
							/>
						</div>

						<div>
							<label className="mb-2 block text-sm font-medium text-[#64748B]">
								Motivo de consulta
							</label>
							<textarea
								value={form.reasonForVisit}
								onChange={(event) =>
									handleChange("reasonForVisit", event.target.value)
								}
								rows={4}
								placeholder="Describa el motivo de la atención..."
								className="w-full resize-none rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
							/>
						</div>

						{/* Bloque actualizado de notas clínicas con selector de plantillas */}
						<div>
							<div className="mb-2 flex items-center justify-between">
								<label className="block text-sm font-medium text-[#64748B]">
									Notas clínicas
								</label>
								{templates.length > 0 && (
									<select
										onChange={handleTemplateSelect}
										defaultValue=""
										className="cursor-pointer rounded-md border border-[#CBD5E1] bg-white px-3 py-1.5 text-sm font-medium text-[#2C7A7B] outline-none transition-colors hover:bg-slate-50 focus:border-[#2C7A7B] focus:ring-1 focus:ring-[#2C7A7B]">
										<option value="" disabled>
											+ Insertar plantilla
										</option>
										{templates.map((template) => (
											<option key={template.id} value={template.id}>
												{template.name}
											</option>
										))}
									</select>
								)}
							</div>
							<textarea
								value={form.clinicalNotes}
								onChange={(event) =>
									handleChange("clinicalNotes", event.target.value)
								}
								rows={8}
								placeholder="Hallazgos, evolución, análisis clínico..."
								className="w-full resize-none rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
							/>
						</div>

						<div>
							<label className="mb-2 block text-sm font-medium text-[#64748B]">
								Plan de manejo
							</label>
							<textarea
								value={form.managementPlan}
								onChange={(event) =>
									handleChange("managementPlan", event.target.value)
								}
								rows={4}
								placeholder="Tratamiento, exámenes, remisiones y seguimiento..."
								className="w-full resize-none rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
							/>
						</div>

						<div className="flex gap-4 pt-4">
							<button
								type="submit"
								disabled={isSaving}
								className="flex-1 rounded-lg bg-[#2C7A7B] py-3 font-semibold text-white transition-colors hover:bg-[#235E5F] disabled:cursor-not-allowed disabled:opacity-70">
								{isSaving ? "Guardando..." : "Guardar Consulta"}
							</button>
							<button
								type="button"
								className="rounded-lg border border-[#CBD5E1] bg-[#F1F5F9] px-8 py-3 font-medium text-[#475569] transition-colors hover:bg-[#E2E8F0]">
								Cancelar
							</button>
						</div>
					</form>
				)}
			</div>
		</div>
	);
}