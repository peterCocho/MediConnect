import { Calendar, Clock, User } from 'lucide-react';

export function AgendaGlobalScreen() {
  const citasGlobal = [
    { hora: '09:00', paciente: 'María González', medico: 'Dr. Ramírez', especialidad: 'Cardiología', estado: 'Confirmada', recepcionista: 'Ana Martínez' },
    { hora: '09:30', paciente: 'Carlos López', medico: 'Dra. López', especialidad: 'Medicina General', estado: 'Confirmada', recepcionista: 'Ana Martínez' },
    { hora: '10:00', paciente: 'Juan Pérez', medico: 'Dra. López', especialidad: 'Medicina General', estado: 'Pendiente', recepcionista: 'Ana Martínez' },
    { hora: '10:30', paciente: 'Ana Martínez', medico: 'Dr. García', especialidad: 'Dermatología', estado: 'Confirmada', recepcionista: 'Pedro Silva' },
    { hora: '11:00', paciente: 'Laura Sánchez', medico: 'Dr. Ramírez', especialidad: 'Cardiología', estado: 'Confirmada', recepcionista: 'Ana Martínez' },
    { hora: '14:00', paciente: 'Carlos Rodríguez', medico: 'Dr. Ramírez', especialidad: 'Cardiología', estado: 'Confirmada', recepcionista: 'Pedro Silva' },
    { hora: '14:30', paciente: 'Sofía Martín', medico: 'Dr. García', especialidad: 'Dermatología', estado: 'Pendiente', recepcionista: 'Ana Martínez' },
    { hora: '15:00', paciente: 'Diego Torres', medico: 'Dra. López', especialidad: 'Medicina General', estado: 'Confirmada', recepcionista: 'Ana Martínez' },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Agenda Global - Panel de Monitorización</h1>
        <p className="text-[#64748B]">Vista de solo lectura de todas las citas del sistema</p>
      </div>

      <div className="grid grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <Calendar className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#64748B] text-sm">Hoy</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">{citasGlobal.length}</h3>
          <p className="text-[#64748B] text-sm">Total de Citas</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#10B981] bg-opacity-20 rounded-lg">
              <Clock className="w-6 h-6 text-[#10B981]" />
            </div>
            <span className="text-[#64748B] text-sm">Estado</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">
            {citasGlobal.filter(c => c.estado === 'Confirmada').length}
          </h3>
          <p className="text-[#64748B] text-sm">Confirmadas</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#F59E0B] bg-opacity-20 rounded-lg">
              <User className="w-6 h-6 text-[#F59E0B]" />
            </div>
            <span className="text-[#64748B] text-sm">Estado</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">
            {citasGlobal.filter(c => c.estado === 'Pendiente').length}
          </h3>
          <p className="text-[#64748B] text-sm">Pendientes</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="p-6 bg-[#F8FAFC] border-b border-[#E2E8F0] rounded-t-xl flex items-center justify-between">
          <h2 className="text-[#1E293B] text-xl font-semibold">Registro Completo de Citas - {new Date().toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Hora</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Paciente</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Médico</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Especialidad</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Agendado por</th>
              </tr>
            </thead>
            <tbody>
              {citasGlobal.map((cita, index) => (
                <tr key={index} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                  <td className="px-6 py-4 text-[#1E293B] text-sm font-medium">{cita.hora}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{cita.paciente}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{cita.medico}</td>
                  <td className="px-6 py-4 text-[#64748B] text-sm">{cita.especialidad}</td>
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
                  <td className="px-6 py-4 text-[#64748B] text-sm">{cita.recepcionista}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="p-6 bg-[#F8FAFC] border-t border-[#E2E8F0] rounded-b-xl">
          <div className="flex items-center gap-2 text-sm text-[#64748B]">
            <span>ℹ️</span>
            <p>
              El administrador no puede crear, modificar o cancelar citas. Esta es una vista de auditoría y monitorización del sistema.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}