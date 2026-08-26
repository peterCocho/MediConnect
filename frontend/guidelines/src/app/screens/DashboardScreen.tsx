import {
  Calendar as CalendarIcon,
  Clock,
  Users,
} from "lucide-react";

export function DashboardScreen() {
  const citasHoy = [
    {
      hora: "09:00",
      paciente: "María González",
      medico: "Dr. Ramírez",
      especialidad: "Cardiología",
      estado: "Confirmada",
    },

    {
      hora: "14:00",
      paciente: "Carlos Rodríguez",
      medico: "Dr. Ramírez",
      especialidad: "Cardiología",
      estado: "Confirmada",
    },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">
          Panel de Control
        </h1>
        <p className="text-[#64748B]">
          Resumen de actividad del día
        </p>
      </div>

      <div className="grid grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <CalendarIcon className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#64748B] text-sm">Hoy</span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">
            12
          </h3>
          <p className="text-[#64748B] text-sm">
            Citas Programadas
          </p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <Clock className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#64748B] text-sm">
              En espera
            </span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">
            3
          </h3>
          <p className="text-[#64748B] text-sm">
            Pacientes Esperando
          </p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#8CD6D1] bg-opacity-20 rounded-lg">
              <Users className="w-6 h-6 text-[#2C7A7B]" />
            </div>
            <span className="text-[#64748B] text-sm">
              Total
            </span>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">
            247
          </h3>
          <p className="text-[#64748B] text-sm">
            Pacientes Activos
          </p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="p-6 bg-[#F8FAFC] border-b border-[#E2E8F0] rounded-t-xl">
          <h2 className="text-[#1E293B] text-xl font-semibold">
            Citas del Día
          </h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">
                  Hora
                </th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">
                  Paciente
                </th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">
                  Médico
                </th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">
                  Especialidad
                </th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">
                  Estado
                </th>
              </tr>
            </thead>
            <tbody>
              {citasHoy.map((cita, index) => (
                <tr
                  key={index}
                  className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]"
                >
                  <td className="px-6 py-4 text-[#1E293B] text-sm">
                    {cita.hora}
                  </td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">
                    {cita.paciente}
                  </td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">
                    {cita.medico}
                  </td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">
                    {cita.especialidad}
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center gap-2 text-sm ${
                        cita.estado === "Confirmada"
                          ? "text-[#10B981]"
                          : "text-[#F59E0B]"
                      }`}
                    >
                      <span
                        className={`w-2 h-2 rounded-full ${
                          cita.estado === "Confirmada"
                            ? "bg-[#10B981]"
                            : "bg-[#F59E0B]"
                        }`}
                      />
                      {cita.estado}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}