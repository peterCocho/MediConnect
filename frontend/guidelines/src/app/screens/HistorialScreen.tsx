export function HistorialScreen() {
  const consultas = [
    {
      fecha: "2024-05-15",
      medico: "Dr. Carlos Ramírez",
      diagnostico: "I10 - Hipertensión Esencial",
      especialidad: "Cardiología",
    },
    {
      fecha: "2024-01-20",
      medico: "Dr. Carlos Ramírez",
      diagnostico: "E11 - Diabetes Mellitus",
      especialidad: "Cardiología",
    },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">
          Historial Clínico
        </h1>
        <p className="text-[#64748B]">
          Registro cronológico de atenciones médicas
        </p>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)] p-8">
        <div className="mb-6 p-4 bg-[#F8FAFC] rounded-lg border border-[#E2E8F0]">
          <h3 className="text-[#1E293B] font-semibold mb-2">
            Paciente: María González García
          </h3>
          <div className="grid grid-cols-3 gap-4 text-sm">
            <div>
              <span className="text-[#64748B]">
                Identificación:
              </span>
              <span className="text-[#1E293B] ml-2">
                1091234567
              </span>
            </div>
            <div>
              <span className="text-[#64748B]">Edad:</span>
              <span className="text-[#1E293B] ml-2">
                39 años
              </span>
            </div>
            <div>
              <span className="text-[#64748B]">
                N° Expediente:
              </span>
              <span className="text-[#1E293B] ml-2">
                HC-1091234567
              </span>
            </div>
          </div>
        </div>

        <div className="relative">
          <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-[#CBD5E1]"></div>

          <div className="space-y-8">
            {consultas.map((consulta, index) => (
              <div key={index} className="relative pl-12">
                <div className="absolute left-0 top-2 w-4 h-4 bg-[#8CD6D1] rounded-full border-2 border-white shadow-md"></div>

                <div className="bg-[#F8FAFC] p-6 rounded-lg border border-[#E2E8F0] hover:border-[#8CD6D1] transition-colors cursor-pointer">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <h4 className="text-[#1E293B] font-semibold mb-1">
                        {consulta.fecha}
                      </h4>
                      <p className="text-[#64748B] text-sm">
                        {consulta.especialidad}
                      </p>
                    </div>
                    <button className="px-4 py-2 bg-white text-[#475569] text-sm border border-[#CBD5E1] rounded hover:bg-[#E2E8F0]">
                      Ver Detalles
                    </button>
                  </div>

                  <div className="space-y-2 text-sm">
                    <div>
                      <span className="text-[#64748B]">
                        Médico:
                      </span>
                      <span className="text-[#1E293B] ml-2">
                        {consulta.medico}
                      </span>
                    </div>
                    <div>
                      <span className="text-[#64748B]">
                        Diagnóstico:
                      </span>
                      <span className="text-[#1E293B] ml-2 font-medium">
                        {consulta.diagnostico}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-8 p-4 bg-[#F8FAFC] rounded-lg border border-[#E2E8F0] text-center">
          <p className="text-[#64748B] text-sm">
            Las consultas anteriores son de solo lectura y no
            pueden ser modificadas
          </p>
        </div>
      </div>
    </div>
  );
}