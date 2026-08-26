export function RegistroPacienteScreen() {
  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Registro de Nuevo Paciente</h1>
        <p className="text-[#64748B]">Paso previo obligatorio antes del agendamiento de citas</p>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)] p-8 max-w-2xl">
        <form className="space-y-6">
          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Documento de Identidad *
            </label>
            <input
              type="text"
              placeholder="Ej: 1091234567"
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
              required
            />
            <p className="text-xs text-[#64748B] mt-1">
              Número de identificación único del paciente
            </p>
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Nombre Completo *
            </label>
            <input
              type="text"
              placeholder="Ej: María González García"
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
              required
            />
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Teléfono *
            </label>
            <input
              type="tel"
              placeholder="Ej: +57 300 123 4567"
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
              required
            />
            <p className="text-xs text-[#64748B] mt-1">
              Requerido para notificaciones automáticas de WhatsApp
            </p>
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Fecha de Nacimiento *
            </label>
            <input
              type="date"
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
              required
            />
          </div>

          <div className="pt-6 border-t border-[#E2E8F0]">
            <div className="p-4 bg-[#8CD6D1] bg-opacity-10 border border-[#8CD6D1] rounded-lg mb-6">
              <p className="text-[#1E293B] text-sm">
                <strong>Nota:</strong> Al registrar el paciente, se creará automáticamente su Historia Clínica con número de expediente único.
              </p>
            </div>

            <div className="flex gap-4">
              <button
                type="submit"
                className="flex-1 bg-[#2C7A7B] text-white font-semibold py-3 rounded-lg hover:bg-[#235E5F] transition-colors"
              >
                Registrar y Aprovisionar
              </button>
              <button
                type="button"
                className="px-8 bg-[#F1F5F9] text-[#475569] font-medium py-3 rounded-lg border border-[#CBD5E1] hover:bg-[#E2E8F0] transition-colors"
              >
                Cancelar
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}