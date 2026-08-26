export function ConsultaScreen() {
  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">
          Consulta Médica Activa
        </h1>
        <p className="text-[#64748B]">
          Registro de atención médica (Modelo Híbrido)
        </p>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)] p-8">
        <div className="mb-8 p-4 bg-[#F8FAFC] rounded-lg border border-[#E2E8F0]">
          <h3 className="text-[#1E293B] font-semibold mb-2">
            Paciente: María González García
          </h3>
          <p className="text-[#64748B] text-sm">
            HC-1091234567 | Cardiología | Dr. Carlos Ramírez
          </p>
        </div>

        <form className="space-y-8">
          <div>
            <h3 className="text-[#1E293B] text-lg font-semibold mb-4 pb-2 border-b-2 border-[#8CD6D1]">
              Signos Vitales (Núcleo Estructurado)
            </h3>
            <div className="grid grid-cols-4 gap-4">
              <div>
                <label className="block text-[#64748B] text-sm font-medium mb-2">
                  Presión Sistólica
                </label>
                <input
                  type="number"
                  placeholder="120"
                  className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
                />
                <span className="text-xs text-[#64748B] mt-1 block">
                  mmHg
                </span>
              </div>

              <div>
                <label className="block text-[#64748B] text-sm font-medium mb-2">
                  Presión Diastólica
                </label>
                <input
                  type="number"
                  placeholder="80"
                  className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
                />
                <span className="text-xs text-[#64748B] mt-1 block">
                  mmHg
                </span>
              </div>

              <div>
                <label className="block text-[#64748B] text-sm font-medium mb-2">
                  Frecuencia Cardíaca
                </label>
                <input
                  type="number"
                  placeholder="75"
                  className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
                />
                <span className="text-xs text-[#64748B] mt-1 block">
                  lpm
                </span>
              </div>

              <div>
                <label className="block text-[#64748B] text-sm font-medium mb-2">
                  Peso
                </label>
                <input
                  type="number"
                  step="0.1"
                  placeholder="70.5"
                  className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
                />
                <span className="text-xs text-[#64748B] mt-1 block">
                  kg
                </span>
              </div>
            </div>
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Diagnóstico CIE-10
            </label>
            <input
              type="text"
              placeholder="Buscar código o descripción..."
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
            />
            <p className="text-xs text-[#64748B] mt-1">
              Ejemplo: I10 - Hipertensión Esencial
            </p>
          </div>

          <div>
            <div className="flex items-center justify-between mb-4 pb-2 border-b-2 border-[#8CD6D1]">
              <h3 className="text-[#1E293B] text-lg font-semibold">
                Notas Clínicas (Flexibilidad)
              </h3>
              <select className="text-sm px-4 py-2 bg-[#2C7A7B] text-white font-medium rounded-md shadow-sm border-none focus:outline-none cursor-pointer">
                <option>Cargar Plantilla Clínica...</option>
                <option>
                  Formato SOAP (Subjetivo, Objetivo, Análisis,
                  Plan)
                </option>
                <option>Control Especialidad</option>
              </select>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-[#64748B] text-sm font-medium">
                Motivo de Consulta
              </label>
              <select className="text-xs px-3 py-1 bg-[#F1F5F9] border border-[#CBD5E1] rounded text-[#475569]">
                <option>Insertar bloque...</option>
                <option>Control de Rutina</option>
                <option>Síntomas Agudos</option>
                <option>Seguimiento</option>
              </select>
            </div>
            <textarea
              rows={4}
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B] resize-none"
              placeholder="S: (Subjetivo) Describa el motivo por el cual el paciente acude a consulta..."
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-[#64748B] text-sm font-medium">
                Notas de Evolución
              </label>
              <select className="text-xs px-3 py-1 bg-[#F1F5F9] border border-[#CBD5E1] rounded text-[#475569]">
                <option>Insertar bloque...</option>
                <option>Evolución Favorable</option>
                <option>Requiere Seguimiento</option>
                <option>Tratamiento Activo</option>
              </select>
            </div>
            <textarea
              rows={6}
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B] resize-none"
              placeholder="O: (Objetivo) Hallazgos del examen físico.&#10;A: (Análisis) Evolución del cuadro clínico..."
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-[#64748B] text-sm font-medium">
                Plan de Manejo
              </label>
              <select className="text-xs px-3 py-1 bg-[#F1F5F9] border border-[#CBD5E1] rounded text-[#475569]">
                <option>Insertar bloque...</option>
                <option>Tratamiento Farmacológico</option>
                <option>Solicitar Exámenes</option>
                <option>Remisión Especialista</option>
              </select>
            </div>
            <textarea
              rows={4}
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B] resize-none"
              placeholder="P: (Plan) Tratamiento prescrito, recomendaciones, próxima cita de control..."
            />
          </div>

          <div className="flex gap-4 pt-4">
            <button
              type="button"
              className="flex-1 bg-[#2C7A7B] text-white font-semibold py-3 rounded-lg hover:bg-[#235E5F] transition-colors"
            >
              Guardar Consulta
            </button>
            <button
              type="button"
              className="px-8 bg-[#F1F5F9] text-[#475569] font-medium py-3 rounded-lg border border-[#CBD5E1] hover:bg-[#E2E8F0] transition-colors"
            >
              Cancelar
            </button>
          </div>

          <div className="p-4 bg-[#EF4444] bg-opacity-10 border border-[#EF4444] rounded-lg">
            <p className="text-[#EF4444] text-sm font-medium">
              ⚠️ Advertencia: Una vez guardada, esta consulta no
              podrá ser modificada (inmutabilidad clínica)
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}