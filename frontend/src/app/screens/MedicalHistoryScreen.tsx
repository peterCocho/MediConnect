import { Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import api from '../../service/api';
import { translateStatus } from '../utils/statusLabels';

type Consultation = {
  id: number;
  consultationDate: string;
  status: string;
  reasonForVisit: string;
  clinicalNotes: string;
  managementPlan: string;
  icd10Code: string;
  doctorId: number;
  medicalRecordId: number;
};

export function MedicalHistoryScreen() {
  const [searchParams] = useSearchParams();
  const [consultations, setConsultations] = useState<Consultation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const medicalRecordId = searchParams.get('medicalRecordId');

  useEffect(() => {
    const loadTimeline = async () => {
      if (!medicalRecordId) {
        setError('No se ha seleccionado un expediente médico para consultar el historial.');
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        const response = await api.get(`/api/consultations/patient-timeline/${medicalRecordId}`);
        setConsultations(response.data ?? []);
        setError('');
      } catch (err) {
        setError('No se pudo cargar el historial clínico.');
      } finally {
        setIsLoading(false);
      }
    };

    loadTimeline();
  }, [medicalRecordId]);

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-2xl font-bold text-[#1E293B] sm:text-3xl">Historial Clínico</h1>
        <p className="text-sm text-[#64748B] sm:text-base">Registro cronológico de atenciones médicas</p>
      </div>

      <div className="rounded-xl border border-[#CBD5E1] bg-white p-5 shadow-[0_2px_4px_rgba(0,0,0,0.05)] sm:p-8">
        {isLoading ? (
          <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
            <Loader2 className="h-5 w-5 animate-spin" />
            <span>Cargando historial clínico...</span>
          </div>
        ) : error ? (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-6 text-center text-red-600">{error}</div>
        ) : (
          <>
            <div className="mb-6 rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] p-4">
              <h3 className="mb-2 font-semibold text-[#1E293B]">Paciente</h3>
              <p className="text-sm text-[#64748B]">Expediente médico #{medicalRecordId ?? 'N/A'}</p>
            </div>

            <div className="relative">
              <div className="absolute bottom-0 left-4 top-0 w-0.5 bg-[#CBD5E1]" />

              <div className="space-y-8">
                {consultations.length === 0 ? (
                  <div className="pl-12 text-[#64748B]">No hay consultas registradas en el historial.</div>
                ) : (
                  consultations.map((consulta) => (
                    <div key={consulta.id} className="relative pl-12">
                      <div className="absolute left-0 top-2 h-4 w-4 rounded-full border-2 border-white bg-[#8CD6D1] shadow-md" />

                      <div className="rounded-lg border border-[#E2E8F0] bg-[#F8FAFC] p-5 hover:border-[#8CD6D1]">
                        <div className="mb-3 flex items-start justify-between gap-3">
                          <div>
                            <h4 className="font-semibold text-[#1E293B]">{new Date(consulta.consultationDate).toLocaleDateString('es-ES')}</h4>
                            <p className="text-sm text-[#64748B]">{translateStatus(consulta.status)}</p>
                          </div>
                        </div>

                        <div className="space-y-2 text-sm">
                          <div>
                            <span className="text-[#64748B]">Médico ID:</span>
                            <span className="ml-2 font-medium text-[#1E293B]">{consulta.doctorId}</span>
                          </div>
                          <div>
                            <span className="text-[#64748B]">Diagnóstico:</span>
                            <span className="ml-2 font-medium text-[#1E293B]">{consulta.icd10Code || 'No registrado'}</span>
                          </div>
                          <div>
                            <span className="text-[#64748B]">Motivo:</span>
                            <span className="ml-2 text-[#1E293B]">{consulta.reasonForVisit || 'Sin motivo registrado'}</span>
                          </div>
                          <div>
                            <span className="text-[#64748B]">Plan:</span>
                            <span className="ml-2 text-[#1E293B]">{consulta.managementPlan || 'Sin plan registrado'}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}