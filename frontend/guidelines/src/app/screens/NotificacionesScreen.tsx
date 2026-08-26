import { Send, Clock, CheckCircle, XCircle } from 'lucide-react';

export function NotificacionesScreen() {
  const notificaciones = [
    {
      id: 1,
      paciente: 'María González García',
      tipo: 'Confirmación',
      estado: 'Enviado',
      fecha: '2024-06-04 10:30',
      mensaje: 'Cita confirmada para Cardiología',
    },
    {
      id: 2,
      paciente: 'Juan Pérez Martínez',
      tipo: 'Recordatorio 24h',
      estado: 'Enviado',
      fecha: '2024-06-04 09:15',
      mensaje: 'Recordatorio de cita para mañana',
    },
    {
      id: 3,
      paciente: 'Ana Martínez López',
      tipo: 'Confirmación',
      estado: 'Pendiente',
      fecha: '2024-06-04 08:45',
      mensaje: 'Esperando envío...',
    },
    {
      id: 4,
      paciente: 'Carlos Rodríguez Silva',
      tipo: 'Recordatorio 24h',
      estado: 'Error',
      fecha: '2024-06-03 18:20',
      mensaje: 'Número de teléfono inválido',
    },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Notificaciones</h1>
          <p className="text-[#64748B]">Panel de mensajes de WhatsApp enviados</p>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#10B981] bg-opacity-20 rounded-lg">
              <CheckCircle className="w-6 h-6 text-[#10B981]" />
            </div>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">156</h3>
          <p className="text-[#64748B] text-sm">Mensajes Enviados</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#F59E0B] bg-opacity-20 rounded-lg">
              <Clock className="w-6 h-6 text-[#F59E0B]" />
            </div>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">3</h3>
          <p className="text-[#64748B] text-sm">Pendientes</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-[#E2E8F0] shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-[#EF4444] bg-opacity-20 rounded-lg">
              <XCircle className="w-6 h-6 text-[#EF4444]" />
            </div>
          </div>
          <h3 className="text-[#1E293B] text-3xl font-bold mb-1">2</h3>
          <p className="text-[#64748B] text-sm">Con Error</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="p-6 bg-[#F8FAFC] border-b border-[#E2E8F0] rounded-t-xl">
          <h2 className="text-[#1E293B] text-xl font-semibold">Historial de Notificaciones</h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Paciente</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Tipo</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Mensaje</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Fecha/Hora</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Acción</th>
              </tr>
            </thead>
            <tbody>
              {notificaciones.map((notif) => (
                <tr key={notif.id} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{notif.paciente}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{notif.tipo}</td>
                  <td className="px-6 py-4 text-[#64748B] text-sm">{notif.mensaje}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{notif.fecha}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center gap-2 text-sm ${
                      notif.estado === 'Enviado' ? 'text-[#10B981]' :
                      notif.estado === 'Pendiente' ? 'text-[#F59E0B]' :
                      'text-[#EF4444]'
                    }`}>
                      <span className={`w-2 h-2 rounded-full ${
                        notif.estado === 'Enviado' ? 'bg-[#10B981]' :
                        notif.estado === 'Pendiente' ? 'bg-[#F59E0B]' :
                        'bg-[#EF4444]'
                      }`} />
                      {notif.estado}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    {notif.estado === 'Error' && (
                      <button className="px-4 py-2 bg-[#2C7A7B] text-white text-sm rounded hover:bg-[#235E5F]">
                        Reintentar
                      </button>
                    )}
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