import { Search } from 'lucide-react';

export function MisPacientesScreen({ onVerHistorial }: { onVerHistorial?: () => void }) {
  const pacientesAsignados = [
    { id: '1091234567', nombre: 'María González García', telefono: '+57 300 123 4567', fechaNacimiento: '1985-03-15', ultimaConsulta: '2024-05-15' },
    { id: '1098765432', nombre: 'Juan Pérez Martínez', telefono: '+57 301 234 5678', fechaNacimiento: '1990-07-22', ultimaConsulta: '2024-05-10' },
    { id: '1087654321', nombre: 'Ana Martínez López', telefono: '+57 302 345 6789', fechaNacimiento: '1978-11-30', ultimaConsulta: '2024-05-08' },
    { id: '1076543210', nombre: 'Carlos Rodríguez Silva', telefono: '+57 303 456 7890', fechaNacimiento: '1995-05-18', ultimaConsulta: '2024-04-20' },
    { id: '1065432109', nombre: 'Laura Sánchez Torres', telefono: '+57 304 567 8901', fechaNacimiento: '1988-09-12', ultimaConsulta: '2024-04-15' },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Mis Pacientes</h1>
        <p className="text-[#64748B]">Pacientes que he atendido - Vista de solo lectura</p>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="p-6 border-b border-[#E2E8F0] flex items-center justify-between">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-[#64748B]" />
            <input
              type="text"
              placeholder="Buscar por nombre o documento..."
              className="w-full pl-10 pr-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
            />
          </div>

          
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Identificación</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Nombre Completo</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Teléfono</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Fecha Nacimiento</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Última Consulta</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Acción</th>
              </tr>
            </thead>
            <tbody>
              {pacientesAsignados.map((paciente) => (
                <tr key={paciente.id} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                  <td className="px-6 py-4 text-[#1E293B] text-sm font-medium">{paciente.id}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{paciente.nombre}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{paciente.telefono}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{paciente.fechaNacimiento}</td>
                  <td className="px-6 py-4 text-[#64748B] text-sm">{paciente.ultimaConsulta}</td>
                  <td className="px-6 py-4">
                    <button
                      onClick={onVerHistorial}
                      className="px-6 py-2 bg-[#2C7A7B] text-white text-sm font-semibold rounded-lg hover:bg-[#235E5F] transition-colors"
                    >
                      Ver Historial
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
              Los médicos no tienen permisos para registrar nuevos pacientes en el sistema. Esta vista muestra únicamente los pacientes que ya ha atendido.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}