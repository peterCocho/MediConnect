export function AdministracionPlantillasScreen() {
  return (
    <div className="flex h-screen bg-[#F4F7F9] font-sans">
      {/* Sidebar Lateral */}
      <aside className="w-64 bg-white border-r border-[#CBD5E1] flex flex-col">
        <div className="p-6 border-b border-[#E2E8F0]">
          <h2 className="text-xl font-bold text-[#1E293B]">
            MediConnect
          </h2>
          <p className="text-xs text-[#64748B]">
            Panel de Administración
          </p>
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <a
            href="#"
            className="block px-4 py-3 text-sm font-medium text-[#64748B] rounded-lg hover:bg-[#F1F5F9]"
          >
            Dashboard
          </a>
          <a
            href="#"
            className="block px-4 py-3 text-sm font-medium text-[#64748B] rounded-lg hover:bg-[#F1F5F9]"
          >
            Gestión de Usuarios
          </a>
          <a
            href="#"
            className="block px-4 py-3 text-sm font-medium text-[#2C7A7B] bg-[#E6F2F2] rounded-lg border-l-4 border-[#2C7A7B]"
          >
            Plantillas Clínicas
          </a>
          <a
            href="#"
            className="block px-4 py-3 text-sm font-medium text-[#64748B] rounded-lg hover:bg-[#F1F5F9]"
          >
            Configuración
          </a>
        </nav>
      </aside>

      {/* Contenido Principal */}
      <main className="flex-1 p-8 overflow-y-auto relative">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-[#1E293B] text-3xl font-bold mb-2">
              Plantillas Clínicas
            </h1>
            <p className="text-[#64748B]">
              Gestione las estructuras base para notas médicas
              por especialidad.
            </p>
          </div>
          <button className="px-6 py-3 bg-[#2C7A7B] text-white text-sm font-semibold rounded-lg hover:bg-[#235E5F] transition-colors shadow-sm">
            + Nueva Plantilla
          </button>
        </div>

        {/* Tabla CRUD */}
        <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)] overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-sm font-semibold text-[#1E293B]">
                  Nombre
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-[#1E293B]">
                  Descripción
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-[#1E293B]">
                  Estado
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-[#1E293B]">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              <tr>
                <td className="px-6 py-4 text-sm font-medium text-[#1E293B]">
                  Formato SOAP
                </td>
                <td className="px-6 py-4 text-sm text-[#64748B]">
                  Estructura general Subjetivo, Objetivo,
                  Análisis, Plan.
                </td>
                <td className="px-6 py-4">
                  <span className="px-3 py-1 bg-[#DEF7EC] text-[#03543F] text-xs font-semibold rounded-full">
                    Activo
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-[#2C7A7B] font-medium cursor-pointer hover:underline">
                  Editar
                </td>
              </tr>
              <tr>
                <td className="px-6 py-4 text-sm font-medium text-[#1E293B]">
                  Control Cardiología
                </td>
                <td className="px-6 py-4 text-sm text-[#64748B]">
                  Seguimiento de hipertensión y riesgo
                  cardiovascular.
                </td>
                <td className="px-6 py-4">
                  <span className="px-3 py-1 bg-[#DEF7EC] text-[#03543F] text-xs font-semibold rounded-full">
                    Activo
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-[#2C7A7B] font-medium cursor-pointer hover:underline">
                  Editar
                </td>
              </tr>
              <tr>
                <td className="px-6 py-4 text-sm font-medium text-[#1E293B]">
                  Nota de Ingreso (Obsoleta)
                </td>
                <td className="px-6 py-4 text-sm text-[#64748B]">
                  Formato antiguo de ingreso general.
                </td>
                <td className="px-6 py-4">
                  <span className="px-3 py-1 bg-[#FDE8E8] text-[#9B1C1C] text-xs font-semibold rounded-full">
                    Inactivo
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-[#2C7A7B] font-medium cursor-pointer hover:underline">
                  Editar
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        {/* Overlay del Modal de Creación (Simulado para Figma) */}
        <div className="absolute inset-0 bg-[#0F172A] bg-opacity-40 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl border border-[#CBD5E1] overflow-hidden">
            <div className="px-6 py-4 border-b border-[#E2E8F0] flex justify-between items-center bg-[#F8FAFC]">
              <h3 className="text-lg font-bold text-[#1E293B]">
                Crear Nueva Plantilla Clínica
              </h3>
              <button className="text-[#64748B] font-bold text-xl hover:text-[#1E293B]">
                ×
              </button>
            </div>

            <div className="p-6 space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[#64748B] text-sm font-medium mb-2">
                    Nombre de la Plantilla
                  </label>
                  <input
                    type="text"
                    placeholder="Ej. Control Odontológico"
                    className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-[#64748B] text-sm font-medium mb-2">
                    Estado Inicial
                  </label>
                  <select className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none">
                    <option>Activo</option>
                    <option>Inactivo</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-[#64748B] text-sm font-medium mb-2">
                  Descripción (Propósito)
                </label>
                <input
                  type="text"
                  placeholder="Breve descripción del caso de uso para esta plantilla..."
                  className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none"
                />
              </div>

              <div>
                <div className="flex justify-between mb-2">
                  <label className="block text-[#64748B] text-sm font-medium">
                    Estructura Base (Texto Plano)
                  </label>
                  <span className="text-xs text-[#94A3B8]">
                    Soporta saltos de línea (\n)
                  </span>
                </div>
                <textarea
                  rows={6}
                  className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none resize-none font-mono text-sm"
                  placeholder="Escriba aquí la estructura. Ej:&#10;Motivo:&#10;Examen físico:&#10;Conducta:"
                />
                <p className="text-xs text-[#EF4444] mt-2 font-medium">
                  Atención: El sistema no soporta etiquetas HTML
                  por directrices de seguridad e integridad del
                  MVP.
                </p>
              </div>
            </div>

            <div className="px-6 py-4 border-t border-[#E2E8F0] bg-[#F8FAFC] flex justify-end gap-3">
              <button className="px-6 py-2 bg-white border border-[#CBD5E1] text-[#475569] font-medium rounded-lg hover:bg-[#F1F5F9] transition-colors">
                Cancelar
              </button>
              <button className="px-6 py-2 bg-[#2C7A7B] text-white font-medium rounded-lg hover:bg-[#235E5F] transition-colors shadow-sm">
                Guardar Plantilla
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}