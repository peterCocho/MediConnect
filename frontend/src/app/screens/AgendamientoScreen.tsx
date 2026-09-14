import { useEffect, useState } from 'react';
import api from '../../service/api';

type PatientOption = {
  id: number;
  fullName: string;
  identityDocument: string;
};

type DoctorOption = {
  id: number;
  fullName: string;
  specialty: string;
};

type AppointmentForm = {
  patientId: string;
  doctorId: string;
  date: string;
  time: string;
};

const toLocalOffsetISOString = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, '0');
  const offsetMinutes = date.getTimezoneOffset();
  const sign = offsetMinutes <= 0 ? '+' : '-';
  const absoluteOffset = Math.abs(offsetMinutes);
  const offsetHours = Math.floor(absoluteOffset / 60);
  const offsetRemainder = absoluteOffset % 60;

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}${sign}${pad(offsetHours)}:${pad(offsetRemainder)}`;
};

const emptyForm: AppointmentForm = {
  patientId: '',
  doctorId: '',
  date: '',
  time: '',
};

export function AgendamientoScreen() {
  const [patients, setPatients] = useState<PatientOption[]>([]);
  const [doctors, setDoctors] = useState<DoctorOption[]>([]);
  const [form, setForm] = useState<AppointmentForm>(emptyForm);
  const [isLoading, setIsLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadOptions = async () => {
      try {
        setIsLoading(true);
        const [patientsResponse, doctorsResponse] = await Promise.all([
          api.get('/api/patients', { params: { page: 0, size: 100, sortBy: 'id' } }),
          api.get('/api/users/doctors', { params: { page: 0, size: 100, sortBy: 'id' } }),
        ]);

        setPatients((patientsResponse.data?.content ?? patientsResponse.data ?? []).map((patient: any) => ({
          id: patient.id,
          fullName: patient.fullName,
          identityDocument: patient.identityDocument,
        })));

        setDoctors((doctorsResponse.data?.content ?? doctorsResponse.data ?? []).map((doctor: any) => ({
          id: doctor.id,
          fullName: doctor.fullName,
          specialty: doctor.specialty,
        })));
      } catch (err) {
        setError('No se pudieron cargar los pacientes o médicos disponibles.');
      } finally {
        setIsLoading(false);
      }
    };

    loadOptions();
  }, []);

  const handleChange = (field: keyof AppointmentForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      if (!form.patientId || !form.doctorId || !form.date || !form.time) {
        throw new Error('Debe completar paciente, médico, fecha y hora.');
      }

      const start = new Date(`${form.date}T${form.time}`);
      if (Number.isNaN(start.getTime())) {
        throw new Error('La fecha y hora seleccionadas no son válidas.');
      }

      const end = new Date(start.getTime() + 60 * 60 * 1000);

      await api.post('/api/appointments/book', {
        patientId: Number(form.patientId),
        doctorId: Number(form.doctorId),
        startTime: toLocalOffsetISOString(start),
        endTime: toLocalOffsetISOString(end),
      });

      setSuccess('Cita agendada correctamente. La cita queda pendiente de confirmación por WhatsApp.');
      setForm(emptyForm);
    } catch (err: any) {
      if (err?.response?.status === 409) {
        setError('El médico ya tiene una cita asignada en este horario o el bloque se encuentra ocupado.');
        return;
      }

      // Extraer estructura exacta de ValidationErrorResponseDTO o ErrorResponseDTO
      if (err?.response?.data) {
        const data = err.response.data;
        if (data.fieldErrors && Object.keys(data.fieldErrors).length > 0) {
          const firstError = Object.values(data.fieldErrors)[0] as string;
          setError(`Validación: ${firstError}`);
        } else {
          setError(data.message || 'No se pudo agendar la cita.');
        }
      } else {
        setError(err?.message || 'Error de conexión con el servidor.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Agendamiento de Citas</h1>
        <p className="text-[#64748B]">Reservar espacio para atención médica</p>
      </div>

      <div className="max-w-2xl rounded-xl border border-[#CBD5E1] bg-white p-8 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        {error && <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
        {success && <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="mb-2 block text-sm font-medium text-[#64748B]">Paciente</label>
            <select
              value={form.patientId}
              onChange={(event) => handleChange('patientId', event.target.value)}
              disabled={isLoading}
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none disabled:opacity-60"
            >
              <option value="">Seleccione un paciente</option>
              {patients.map((patient) => (
                <option key={patient.id} value={patient.id}>
                  {patient.fullName} ({patient.identityDocument})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium text-[#64748B]">Médico</label>
            <select
              value={form.doctorId}
              onChange={(event) => handleChange('doctorId', event.target.value)}
              disabled={isLoading}
              className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none disabled:opacity-60"
            >
              <option value="">Seleccione un médico</option>
              {doctors.map((doctor) => (
                <option key={doctor.id} value={doctor.id}>
                  {doctor.fullName} - {doctor.specialty}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-2 block text-sm font-medium text-[#64748B]">Fecha</label>
              <input
                type="date"
                value={form.date}
                onChange={(event) => handleChange('date', event.target.value)}
                className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#64748B]">Hora</label>
              <input
                type="time"
                value={form.time}
                onChange={(event) => handleChange('time', event.target.value)}
                className="w-full rounded-md border-[1.5px] border-[#94A3B8] bg-[#F8FAFC] px-4 py-3 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
              />
            </div>
          </div>

          <div className="rounded-lg border border-[#8CD6D1] bg-[#8CD6D1] bg-opacity-10 p-4 text-sm text-[#1E293B]">
            La cita queda programada en estado pendiente de confirmación por WhatsApp del paciente; la recepción no confirma ni cancela directamente desde esta pantalla.
          </div>

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={submitting || isLoading}
              className="flex-1 rounded-lg bg-[#2C7A7B] py-3 font-semibold text-white transition-colors hover:bg-[#235E5F] disabled:opacity-60"
            >
              {submitting ? 'Agendando...' : 'Agendar cita'}
            </button>
            <button
              type="button"
              onClick={() => setForm(emptyForm)}
              className="rounded-lg border border-[#CBD5E1] bg-[#F1F5F9] px-8 py-3 font-medium text-[#475569] transition-colors hover:bg-[#E2E8F0]"
            >
              Limpiar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}