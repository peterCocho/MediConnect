import { useState } from 'react';
import api from '../../service/api';

type PatientForm = {
  identityDocument: string;
  fullName: string;
  phone: string;
  birthDate: string;
};

const emptyForm: PatientForm = {
  identityDocument: '',
  fullName: '',
  phone: '',
  birthDate: '',
};

export function RegistroPacienteScreen() {
  const [form, setForm] = useState<PatientForm>(emptyForm);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (field: keyof PatientForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await api.post('/api/patients', form);
      setSuccess('Paciente registrado correctamente.');
      setForm(emptyForm);
    } catch (err: any) {
      const message = err?.response?.data?.message || 'No se pudo registrar el paciente.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Registro de Nuevo Paciente</h1>
        <p className="text-[#64748B]">Paso previo obligatorio antes del agendamiento de citas</p>
      </div>

      <div className="max-w-2xl rounded-xl border border-[#CBD5E1] bg-white p-8 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        {error && <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
        {success && <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="mb-2 block text-sm font-medium text-[#64748B]">
              Documento de Identidad *
            </label>
            <input
              type="text"
              value={form.identityDocument}
              onChange={(event) => handleChange('identityDocument', event.target.value)}
              placeholder="Ej: 1091234567"
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
              required
            />
            <p className="mt-1 text-xs text-[#64748B]">Número de identificación único del paciente</p>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#64748B]">
              Nombre Completo *
            </label>
            <input
              type="text"
              value={form.fullName}
              onChange={(event) => handleChange('fullName', event.target.value)}
              placeholder="Ej: María González García"
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#64748B]">
              Teléfono *
            </label>
            <input
              type="tel"
              value={form.phone}
              onChange={(event) => handleChange('phone', event.target.value)}
              placeholder="Ej: +57 300 123 4567"
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
              required
            />
            <p className="mt-1 text-xs text-[#64748B]">Requerido para notificaciones automáticas de WhatsApp</p>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#64748B]">
              Fecha de Nacimiento *
            </label>
            <input
              type="date"
              value={form.birthDate}
              onChange={(event) => handleChange('birthDate', event.target.value)}
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
              required
            />
          </div>

          <div className="border-t border-[#E2E8F0] pt-6">
            <div className="mb-6 rounded-lg border border-[#8CD6D1] bg-[#8CD6D1] bg-opacity-10 p-4">
              <p className="text-sm text-[#1E293B]">
                <strong>Nota:</strong> Al registrar el paciente, se creará automáticamente su Historia Clínica con número de expediente único.
              </p>
            </div>

            <div className="flex gap-4">
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 rounded-lg bg-[#2C7A7B] py-3 font-semibold text-white transition-colors hover:bg-[#235E5F] disabled:opacity-60"
              >
                {submitting ? 'Registrando...' : 'Registrar y Aprovisionar'}
              </button>
              <button
                type="button"
                onClick={() => setForm(emptyForm)}
                className="rounded-lg border border-[#CBD5E1] bg-[#F1F5F9] px-8 py-3 font-medium text-[#475569] transition-colors hover:bg-[#E2E8F0]"
              >
                Cancelar
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}