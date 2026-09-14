import { Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';

type WhatsappMessage = { id: number; phoneNumber: string; messageBody: string; receivedAt: string; };
type ErrorLog = { id: number; timestamp: string; exceptionType: string; message: string; path: string; };
type AppointmentDTO = { id: number; patientId: number; startTime: string; }; // Ajusta según tu DTO

type ActiveTab = 'UNREAD' | 'PENDING' | 'ERRORS';

export function NotificacionesScreen() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('UNREAD');
  
  const [messages, setMessages] = useState<WhatsappMessage[]>([]);
  const [pendingApps, setPendingApps] = useState<AppointmentDTO[]>([]);
  const [errors, setErrors] = useState<ErrorLog[]>([]);
  
  const [isLoading, setIsLoading] = useState(true);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [msgRes, pendRes, errRes] = await Promise.allSettled([
        api.get('/api/whatsapp/messages/unread'),
        api.get('/api/appointments/pending-confirmation'),
        api.get('/api/errors') // El nuevo endpoint que devuelve la lista
      ]);

      if (msgRes.status === 'fulfilled') setMessages(msgRes.value.data ?? []);
      if (pendRes.status === 'fulfilled') setPendingApps(pendRes.value.data ?? []);
      if (errRes.status === 'fulfilled') setErrors(errRes.value.data ?? []);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const markAsRead = async (id: number) => {
    try {
      await api.patch(`/api/whatsapp/messages/${id}/read`);
      setMessages((current) => current.filter((m) => m.id !== id));
    } catch (err) { console.error('Error al marcar leído'); }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Notificaciones</h1>
        <p className="text-[#64748B]">Centro de control operativo y alertas</p>
      </div>

      <div className="mb-8 grid grid-cols-3 gap-6">
        {/* Tarjeta: Sin Leer */}
        <div 
          onClick={() => setActiveTab('UNREAD')}
          className={`cursor-pointer rounded-xl border p-6 shadow-sm transition-all ${activeTab === 'UNREAD' ? 'border-[#10B981] ring-2 ring-[#10B981] ring-opacity-20' : 'border-[#E2E8F0] bg-white'}`}>
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#10B981] bg-opacity-20 p-3"><CheckCircle className="h-6 w-6 text-[#10B981]" /></div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{messages.length}</h3>
          <p className="text-sm text-[#64748B]">Sin leer</p>
        </div>

        {/* Tarjeta: Pendientes */}
        <div 
          onClick={() => setActiveTab('PENDING')}
          className={`cursor-pointer rounded-xl border p-6 shadow-sm transition-all ${activeTab === 'PENDING' ? 'border-[#F59E0B] ring-2 ring-[#F59E0B] ring-opacity-20' : 'border-[#E2E8F0] bg-white'}`}>
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#F59E0B] bg-opacity-20 p-3"><Clock className="h-6 w-6 text-[#F59E0B]" /></div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{pendingApps.length}</h3>
          <p className="text-sm text-[#64748B]">Por revisar (Citas Pendientes)</p>
        </div>

        {/* Tarjeta: Errores */}
        <div 
          onClick={() => setActiveTab('ERRORS')}
          className={`cursor-pointer rounded-xl border p-6 shadow-sm transition-all ${activeTab === 'ERRORS' ? 'border-[#EF4444] ring-2 ring-[#EF4444] ring-opacity-20' : 'border-[#E2E8F0] bg-white'}`}>
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#EF4444] bg-opacity-20 p-3"><XCircle className="h-6 w-6 text-[#EF4444]" /></div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{errors.length}</h3>
          <p className="text-sm text-[#64748B]">Con error de sistema</p>
        </div>
      </div>

      {/* Renderizado condicional de la tabla según la pestaña activa */}
      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-sm">
        <div className="rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
          <h2 className="text-xl font-semibold text-[#1E293B]">
            {activeTab === 'UNREAD' ? 'Mensajes de WhatsApp' : activeTab === 'PENDING' ? 'Citas pendientes de confirmación' : 'Registro de errores del sistema'}
          </h2>
        </div>

        {isLoading ? (
          <div className="py-12 text-center text-[#64748B]"><Loader2 className="mx-auto h-5 w-5 animate-spin" /></div>
        ) : (
          <div className="overflow-x-auto p-4">
            {/* Tabla Mensajes */}
            {activeTab === 'UNREAD' && (
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr><th className="p-4">Teléfono</th><th className="p-4">Mensaje</th><th className="p-4">Fecha</th><th className="p-4">Acción</th></tr>
                </thead>
                <tbody>
                  {messages.map(m => (
                    <tr key={m.id} className="border-b">
                      <td className="p-4">{m.phoneNumber}</td>
                      <td className="p-4">{m.messageBody}</td>
                      <td className="p-4">{new Date(m.receivedAt).toLocaleString('es-ES')}</td>
                      <td className="p-4"><button onClick={() => markAsRead(m.id)} className="rounded bg-[#2C7A7B] px-3 py-1 text-white">Leído</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {/* Tabla Citas Pendientes */}
            {activeTab === 'PENDING' && (
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr><th className="p-4">ID Cita</th><th className="p-4">Fecha Programada</th><th className="p-4">Estado</th></tr>
                </thead>
                <tbody>
                  {pendingApps.map(a => (
                    <tr key={a.id} className="border-b">
                      <td className="p-4">#{a.id}</td>
                      <td className="p-4">{new Date(a.startTime).toLocaleString('es-ES')}</td>
                      <td className="p-4"><span className="rounded bg-orange-100 px-2 py-1 text-orange-800">Pendiente de Confirmación</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {/* Tabla Errores */}
            {activeTab === 'ERRORS' && (
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 text-gray-500">
                  <tr><th className="p-4">Fecha</th><th className="p-4">Tipo</th><th className="p-4">Mensaje</th><th className="p-4">Ruta</th></tr>
                </thead>
                <tbody>
                  {errors.map(e => (
                    <tr key={e.id} className="border-b">
                      <td className="p-4">{new Date(e.timestamp).toLocaleString('es-ES')}</td>
                      <td className="p-4 font-mono text-red-600">{e.exceptionType}</td>
                      <td className="p-4">{e.message}</td>
                      <td className="p-4 text-gray-400">{e.path}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  );
}