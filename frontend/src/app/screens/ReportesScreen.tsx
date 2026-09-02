import { BarChart3, Loader2, TrendingUp } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';

type DashboardMetric = {
  totalCompletedConsultations: number;
  topDiagnoses: Array<{ icd10Code: string; occurrences: number }>;
};

export function ReportesScreen() {
  const [metrics, setMetrics] = useState<DashboardMetric | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadReport = async () => {
      try {
        setIsLoading(true);
        const response = await api.get('/api/reports/dashboard');
        setMetrics(response.data ?? null);
        setError('');
      } catch (err) {
        setError('No se pudo cargar el reporte del sistema.');
      } finally {
        setIsLoading(false);
      }
    };

    loadReport();
  }, []);

  // Validación para evitar división por cero si no hay consultas
  const totalConsultations = metrics?.totalCompletedConsultations && metrics.totalCompletedConsultations > 0 
    ? metrics.totalCompletedConsultations 
    : 1;

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-2xl font-bold text-[#1E293B] sm:text-3xl">Reportes</h1>
        <p className="text-sm text-[#64748B] sm:text-base">Análisis y estadísticas del sistema</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-3 rounded-xl border border-[#CBD5E1] bg-white py-12 text-[#64748B] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Cargando métricas...</span>
        </div>
      ) : error ? (
        <div className="rounded-xl border border-red-200 bg-red-50 px-6 py-8 text-center text-red-600">{error}</div>
      ) : (
        <>
          <div className="mb-8 grid gap-6 md:grid-cols-2">
            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <BarChart3 className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#64748B]">Histórico</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics?.totalCompletedConsultations ?? 0}</h3>
              <p className="text-sm text-[#64748B]">Total de Consultas</p>
            </div>

            <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
              <div className="mb-4 flex items-center justify-between">
                <div className="rounded-lg bg-[#8CD6D1] bg-opacity-20 p-3">
                  <TrendingUp className="h-6 w-6 text-[#2C7A7B]" />
                </div>
                <span className="text-sm text-[#10B981]">Sistema</span>
              </div>
              <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{metrics?.topDiagnoses?.length ?? 0}</h3>
              <p className="text-sm text-[#64748B]">Diagnósticos principales</p>
            </div>
          </div>

          <div className="rounded-xl border border-[#CBD5E1] bg-white p-6 shadow-[0_2px_4px_rgba(0,0,0,0.05)] sm:p-8">
            <h2 className="mb-6 text-xl font-semibold text-[#1E293B]">Diagnósticos Más Frecuentes (CIE-10)</h2>

            <div className="space-y-4">
              {(metrics?.topDiagnoses ?? []).map((item) => (
                <div key={item.icd10Code} className="flex items-center gap-4">
                  <div className="w-24 text-sm font-medium text-[#64748B]">{item.icd10Code}</div>
                  <div className="flex-1">
                    <div className="mb-1 flex items-center justify-end">
                      <span className="text-sm font-semibold text-[#64748B]">{item.occurrences} {item.occurrences === 1 ? 'caso' : 'casos'}</span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-[#F1F5F9]">
                      <div
                        className="h-full bg-[#2C7A7B] transition-all duration-500 ease-in-out"
                        style={{ width: `${(item.occurrences / totalConsultations) * 100}%` }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}