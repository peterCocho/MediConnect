import { Calendar, Clock } from 'lucide-react';

export function MiAgendaScreen({ onIniciarConsulta }: { onIniciarConsulta?: () => void }) {
  // Solo citas del Dr. Carlos Ramírez - Privacidad de datos
  const citasHoy = [
    { hora: '09:00', paciente: 'María González García', identificacion: '1091234567', estado: 'Confirmada', medico: 'Dr. Ramírez' },
    { hora: '11:00', paciente: 'Laura Sánchez Torres', identificacion: '1065432109', estado: 'Confirmada', medico: 'Dr. Ramírez' },
    { hora: '14:00', paciente: 'Carlos Rodríguez Silva', identificacion: '1076543210', estado: 'Confirmada', medico: 'Dr. Ramírez' },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Mi Agenda</h1>
        <p className="text-[#64748B]">Citas programadas para hoy - {new Date().toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
      </div>

      <div className="grid grid-cols-2 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <Calendar className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#64748B] text-sm">Hoy</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">{citasHoy.length}</h3>
          <p className="text-[#64748B] text-sm">Pacientes Programados</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#10B981] bg-opacity-20 rounded-lg">
              <Clock className="w-6 h-6 text-[#10B981]" />
            </div>
            <span className="text-[#64748B] text-sm">Estado</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">
            {citasHoy.filter(c => c.estado === 'Confirmada').length}
          </h3>
          <p className="text-[#64748B] text-sm">Confirmadas</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="p-6 bg-[#F8FAFC] border-b border-[#E2E8F0] rounded-t-xl">
          <h2 className="text-[#1E293B] text-xl font-semibold">Listado Secuencial de Pacientes</h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Hora</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Nombre del Paciente</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Identificación</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Acción</th>
              </tr>
            </thead>
            <tbody>
              {citasHoy.map((cita, index) => (
                <tr key={index} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                  <td className="px-6 py-4 text-[#1E293B] text-sm font-semibold">{cita.hora}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{cita.paciente}</td>
                  <td className="px-6 py-4 text-[#64748B] text-sm">{cita.identificacion}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center gap-2 text-sm ${
                      cita.estado === 'Confirmada' ? 'text-[#10B981]' : 'text-[#F59E0B]'
                    }`}>
                      <span className={`w-2 h-2 rounded-full ${
                        cita.estado === 'Confirmada' ? 'bg-[#10B981]' : 'bg-[#F59E0B]'
                      }`} />
                      {cita.estado}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button
                      onClick={onIniciarConsulta}
                      className="px-6 py-2 bg-[#2C7A7B] text-white text-sm font-semibold rounded-lg hover:bg-[#235E5F] transition-colors"
                    >
                      Iniciar Consulta
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="p-6 bg-[#F8FAFC] border-t border-[#E2E8F0] rounded-b-xl">
          <div className="flex items-center gap-2 text-sm text-[#64748B]">
            <span>ℹ️</span>
            <p>
              El botón "Iniciar Consulta" abrirá el registro clínico del paciente. Los médicos no gestionan citas administrativas.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}