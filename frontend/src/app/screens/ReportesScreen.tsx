import { BarChart3, TrendingUp, Users, Calendar } from 'lucide-react';

export function ReportesScreen() {
  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Reportes</h1>
        <p className="text-[#64748B]">Análisis y estadísticas del sistema</p>
      </div>

      <div className="grid grid-cols-2 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <BarChart3 className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#64748B] text-sm">Este mes</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">324</h3>
          <p className="text-[#64748B] text-sm">Total de Consultas</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <TrendingUp className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#10B981] text-sm">+12%</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">89%</h3>
          <p className="text-[#64748B] text-sm">Tasa de Asistencia</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)] p-8">
        <h2 className="text-[#1E293B] text-xl font-semibold mb-6">Diagnósticos Más Frecuentes (CIE-10)</h2>

        <div className="space-y-4">
          {[
            { codigo: 'I10', diagnostico: 'Hipertensión Esencial', cantidad: 45 },
            { codigo: 'E11', diagnostico: 'Diabetes Mellitus', cantidad: 38 },
            { codigo: 'J06.9', diagnostico: 'Infección Respiratoria', cantidad: 32 },
            { codigo: 'L30.9', diagnostico: 'Dermatitis', cantidad: 28 },
          ].map((item, index) => (
            <div key={index} className="flex items-center gap-4">
              <div className="w-24 text-[#64748B] text-sm font-medium">{item.codigo}</div>
              <div className="flex-1">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-[#1E293B] text-sm">{item.diagnostico}</span>
                  <span className="text-[#64748B] text-sm font-semibold">{item.cantidad}</span>
                </div>
                <div className="h-2 bg-[#F1F5F9] rounded-full overflow-hidden">
                  <div
                    className="h-full bg-[#2C7A7B]"
                    style={{ width: `${(item.cantidad / 45) * 100}%` }}
                  />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}