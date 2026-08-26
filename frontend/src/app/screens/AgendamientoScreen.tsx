export function AgendamientoScreen() {
  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8">
        <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Agendamiento de Citas</h1>
        <p className="text-[#64748B]">Reserve un espacio para atención médica</p>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)] p-8">
        <form className="space-y-6 max-w-2xl">
          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Especialidad
            </label>
            <select className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]">
              <option>Seleccione una especialidad</option>
              <option>Cardiología</option>
              <option>Medicina General</option>
              <option>Dermatología</option>
              <option>Ortopedia</option>
              <option>Ginecología</option>
            </select>
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Médico
            </label>
            <select className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]">
              <option>Seleccione un médico</option>
              <option>Dr. Carlos Ramírez - Cardiología</option>
              <option>Dra. María López - Medicina General</option>
              <option>Dr. José García - Dermatología</option>
            </select>
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Paciente
            </label>
            <input
              type="text"
              placeholder="Buscar paciente por nombre o documento..."
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-[#64748B] text-sm font-medium mb-2">
                Fecha
              </label>
              <input
                type="date"
                className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]"
              />
            </div>

            <div>
              <label className="block text-[#64748B] text-sm font-medium mb-2">
                Hora
              </label>
              <select className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B]">
                <option>09:00 AM</option>
                <option>10:00 AM</option>
                <option>11:00 AM</option>
                <option>02:00 PM</option>
                <option>03:00 PM</option>
                <option>04:00 PM</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-[#64748B] text-sm font-medium mb-2">
              Motivo de la Consulta (Opcional)
            </label>
            <textarea
              rows={3}
              className="w-full px-4 py-3 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-md text-[#1E293B] focus:outline-none focus:border-[#2C7A7B] resize-none"
              placeholder="Breve descripción del motivo..."
            />
          </div>

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              className="flex-1 bg-[#2C7A7B] text-white font-semibold py-3 rounded-lg hover:bg-[#235E5F] transition-colors"
            >
              Confirmar Cita
            </button>
            <button
              type="button"
              className="px-8 bg-[#F1F5F9] text-[#475569] font-medium py-3 rounded-lg border border-[#CBD5E1] hover:bg-[#E2E8F0] transition-colors"
            >
              Cancelar
            </button>
          </div>

          <div className="mt-6 p-4 bg-[#8CD6D1] bg-opacity-10 border border-[#8CD6D1] rounded-lg">
            <p className="text-[#1E293B] text-sm">
              <strong>Nota:</strong> Al confirmar, se enviará automáticamente un mensaje de WhatsApp al paciente con los detalles de la cita.
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}