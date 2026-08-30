import { Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';

type WhatsappMessage = {
  id: number;
  phoneNumber: string;
  messageBody: string;
  receivedAt: string;
};

export function NotificacionesScreen() {
  const [messages, setMessages] = useState<WhatsappMessage[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const loadMessages = async () => {
    try {
      setIsLoading(true);
      setError('');
      const response = await api.get('/api/whatsapp/messages/unread');
      setMessages(response.data ?? []);
    } catch (err) {
      setError('No se pudieron cargar las notificaciones de WhatsApp.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadMessages();
  }, []);

  const markAsRead = async (id: number) => {
    try {
      await api.patch(`/api/whatsapp/messages/${id}/read`);
      setMessages((current) => current.filter((message) => message.id !== id));
    } catch (err) {
      setError('No se pudo marcar el mensaje como leído.');
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-8">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Notificaciones</h1>
          <p className="text-[#64748B]">Mensajes no leídos recibidos por WhatsApp</p>
        </div>
      </div>

      <div className="mb-8 grid grid-cols-3 gap-6">
        <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#10B981] bg-opacity-20 p-3">
              <CheckCircle className="h-6 w-6 text-[#10B981]" />
            </div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{messages.length}</h3>
          <p className="text-sm text-[#64748B]">Sin leer</p>
        </div>

        <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#F59E0B] bg-opacity-20 p-3">
              <Clock className="h-6 w-6 text-[#F59E0B]" />
            </div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{messages.length > 0 ? 'Nuevos' : '0'}</h3>
          <p className="text-sm text-[#64748B]">Por revisar</p>
        </div>

        <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-[0_4px_10px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#EF4444] bg-opacity-20 p-3">
              <XCircle className="h-6 w-6 text-[#EF4444]" />
            </div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">0</h3>
          <p className="text-sm text-[#64748B]">Con error</p>
        </div>
      </div>

      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="rounded-t-xl border-b border-[#E2E8F0] bg-[#F8FAFC] p-6">
          <h2 className="text-xl font-semibold text-[#1E293B]">Historial de notificaciones</h2>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
            <Loader2 className="h-5 w-5 animate-spin" />
            <span>Cargando mensajes...</span>
          </div>
        ) : error ? (
          <div className="px-6 py-8 text-center text-red-600">{error}</div>
        ) : messages.length === 0 ? (
          <div className="px-6 py-10 text-center text-[#64748B]">No hay mensajes de WhatsApp pendientes.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Teléfono</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Mensaje</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Fecha/Hora</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-[#64748B]">Acción</th>
                </tr>
              </thead>
              <tbody>
                {messages.map((message) => (
                  <tr key={message.id} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                    <td className="px-6 py-4 text-sm text-[#1E293B]">{message.phoneNumber}</td>
                    <td className="px-6 py-4 text-sm text-[#64748B]">{message.messageBody}</td>
                    <td className="px-6 py-4 text-sm text-[#1E293B]">{new Date(message.receivedAt).toLocaleString('es-ES')}</td>
                    <td className="px-6 py-4">
                      <button type="button" onClick={() => markAsRead(message.id)} className="rounded bg-[#2C7A7B] px-4 py-2 text-sm text-white hover:bg-[#235E5F]">
                        Marcar como leída
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}