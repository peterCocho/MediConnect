import { Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';

type WhatsappMessage = { id: number; phoneNumber: string; messageBody: string; receivedAt: string; };
type ErrorLog = { id: number; timestamp: string; exceptionType: string; message: string; path: string; };
type AppointmentDTO = { id: number; patientId: number; startTime: string; };

type ActiveTab = 'UNREAD' | 'PENDING' | 'ERRORS';

export function NotificacionesScreen() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('UNREAD');
  
  const [messages, setMessages] = useState<WhatsappMessage[]>([]);
  const [pendingApps, setPendingApps] = useState<AppointmentDTO[]>([]);
  const [errors, setErrors] = useState<ErrorLog[]>([]);
  const [errorCount, setErrorCount] = useState<number>(0);
  
  // Pagination states for the errors table
  const [errorPage, setErrorPage] = useState<number>(0);
  const [errorTotalPages, setErrorTotalPages] = useState<number>(0);
  
  const [isLoading, setIsLoading] = useState(true);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [msgRes, pendRes, errRes, countRes] = await Promise.allSettled([
        api.get('/api/whatsapp/messages/unread'),
        api.get('/api/appointments/pending-confirmation'),
        // Request page 0 on initial load
        api.get('/api/errors', { params: { page: 0, size: 10 } }),
        api.get('/api/errors/count')
      ]);

      if (msgRes.status === 'fulfilled') setMessages(msgRes.value.data ?? []);
      if (pendRes.status === 'fulfilled') setPendingApps(pendRes.value.data ?? []);
      if (errRes.status === 'fulfilled') {
        // Extract array from the 'content' property of the Page object
        setErrors(errRes.value.data?.content ?? []);
        setErrorTotalPages(errRes.value.data?.totalPages ?? 0);
        setErrorPage(0);
      }
      if (countRes.status === 'fulfilled') setErrorCount(countRes.value.data?.count ?? 0);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  // Fetches a specific page of errors without reloading the entire screen
  const fetchErrorPage = async (newPage: number) => {
    try {
      const res = await api.get('/api/errors', { params: { page: newPage, size: 10 } });
      setErrors(res.data?.content ?? []);
      setErrorTotalPages(res.data?.totalPages ?? 0);
      setErrorPage(newPage);
    } catch (err) {
      console.error(err);
    }
  };

  const markAsRead = async (id: number) => {
    try {
      await api.patch(`/api/whatsapp/messages/${id}/read`);
      setMessages((current) => current.filter((m) => m.id !== id));
    } catch (err) { 
      alert('Error al marcar el mensaje como leído. Verifica tu sesión o conexión.');
    }
  };

  // Removes the error log from the database and updates local state/counters
  const dismissError = async (id: number) => {
    try {
      await api.delete(`/api/errors/${id}`);
      setErrors((current) => current.filter((e) => e.id !== id));
      setErrorCount((current) => Math.max(0, current - 1));
      
      // If the current page becomes empty and it's not the first page, go back one page
      if (errors.length === 1 && errorPage > 0) {
        fetchErrorPage(errorPage - 1);
      }
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 401) {
        alert('Error 401: Sesión expirada. Por favor, cierra sesión y vuelve a ingresar.');
      } else if (status === 403) {
        alert('Error 403: Permisos insuficientes para descartar errores.');
      } else {
        alert(`Fallo al descartar el error. Código de estado: ${status || 'Desconocido'}`);
      }
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 md:p-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">Notificaciones</h1>
        <p className="text-[#64748B]">Centro de control operativo y alertas</p>
      </div>

      <div className="mb-8 grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6">
        {/* Unread Messages Card */}
        <div 
          onClick={() => setActiveTab('UNREAD')}
          className={`cursor-pointer rounded-xl border p-6 shadow-sm transition-all ${activeTab === 'UNREAD' ? 'border-[#10B981] ring-2 ring-[#10B981] ring-opacity-20 bg-white' : 'border-[#E2E8F0] bg-white hover:bg-gray-50'}`}>
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#10B981] bg-opacity-20 p-3"><CheckCircle className="h-6 w-6 text-[#10B981]" /></div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{messages.length}</h3>
          <p className="text-sm text-[#64748B]">Sin leer</p>
        </div>

        {/* Pending Appointments Card */}
        <div 
          onClick={() => setActiveTab('PENDING')}
          className={`cursor-pointer rounded-xl border p-6 shadow-sm transition-all ${activeTab === 'PENDING' ? 'border-[#F59E0B] ring-2 ring-[#F59E0B] ring-opacity-20 bg-white' : 'border-[#E2E8F0] bg-white hover:bg-gray-50'}`}>
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#F59E0B] bg-opacity-20 p-3"><Clock className="h-6 w-6 text-[#F59E0B]" /></div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{pendingApps.length}</h3>
          <p className="text-sm text-[#64748B]">Por revisar (Citas)</p>
        </div>

        {/* System Errors Card */}
        <div 
          onClick={() => setActiveTab('ERRORS')}
          className={`cursor-pointer rounded-xl border p-6 shadow-sm transition-all ${activeTab === 'ERRORS' ? 'border-[#EF4444] ring-2 ring-[#EF4444] ring-opacity-20 bg-white' : 'border-[#E2E8F0] bg-white hover:bg-gray-50'}`}>
          <div className="mb-4 flex items-center justify-between">
            <div className="rounded-lg bg-[#EF4444] bg-opacity-20 p-3"><XCircle className="h-6 w-6 text-[#EF4444]" /></div>
          </div>
          <h3 className="mb-1 text-3xl font-bold text-[#1E293B]">{errorCount}</h3>
          <p className="text-sm text-[#64748B]">Con error de sistema</p>
        </div>
      </div>

      <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-sm overflow-hidden">
        <div className="border-b border-[#E2E8F0] bg-[#F8FAFC] p-4 md:p-6">
          <h2 className="text-lg md:text-xl font-semibold text-[#1E293B]">
            {activeTab === 'UNREAD' ? 'Mensajes de WhatsApp' : activeTab === 'PENDING' ? 'Citas pendientes de confirmación' : 'Registro de errores del sistema'}
          </h2>
        </div>

        {isLoading ? (
          <div className="py-12 text-center text-[#64748B]"><Loader2 className="mx-auto h-5 w-5 animate-spin" /></div>
        ) : (
          <div className="p-0 md:p-4">
            
            {/* Table: Unread Messages */}
            {activeTab === 'UNREAD' && (
              <table className="w-full">
                <thead className="hidden md:table-header-group bg-gray-50 text-gray-500">
                  <tr>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Teléfono</th>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Mensaje</th>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Fecha</th>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {messages.length === 0 ? (
                    <tr><td colSpan={4} className="p-6 text-center text-[#64748B] block md:table-cell">No hay mensajes.</td></tr>
                  ) : messages.map(m => (
                    <tr key={m.id} className="block border-b border-[#E2E8F0] bg-white p-4 md:table-row md:p-0 hover:bg-[#F8FAFC]">
                      <td className="flex items-center justify-between py-2 text-sm text-[#1E293B] before:content-[attr(data-label)] before:font-medium before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="Teléfono">{m.phoneNumber}</td>
                      <td className="flex flex-col py-2 text-sm text-[#64748B] before:content-[attr(data-label)] before:font-medium before:text-[#64748B] before:mb-1 md:table-cell md:px-6 md:py-4 md:before:hidden md:max-w-xs break-words" data-label="Mensaje">{m.messageBody}</td>
                      <td className="flex items-center justify-between py-2 text-sm text-[#1E293B] before:content-[attr(data-label)] before:font-medium before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="Fecha">{new Date(m.receivedAt).toLocaleString('es-ES')}</td>
                      <td className="flex items-center justify-end py-3 md:py-4 md:table-cell md:px-6" data-label="Acción">
                        <button onClick={() => markAsRead(m.id)} className="w-full md:w-auto rounded bg-[#2C7A7B] px-4 py-2 text-sm font-medium text-white hover:bg-[#235E5F]">Leído</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {/* Table: Pending Appointments */}
            {activeTab === 'PENDING' && (
              <table className="w-full">
                <thead className="hidden md:table-header-group bg-gray-50 text-gray-500">
                  <tr>
                    <th className="px-6 py-4 text-left text-sm font-semibold">ID Cita</th>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Fecha Programada</th>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingApps.length === 0 ? (
                    <tr><td colSpan={3} className="p-6 text-center text-[#64748B] block md:table-cell">No hay citas pendientes.</td></tr>
                  ) : pendingApps.map(a => (
                    <tr key={a.id} className="block border-b border-[#E2E8F0] bg-white p-4 md:table-row md:p-0 hover:bg-[#F8FAFC]">
                      <td className="flex items-center justify-between py-2 text-sm text-[#1E293B] before:content-[attr(data-label)] before:font-medium before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="ID Cita">#{a.id}</td>
                      <td className="flex items-center justify-between py-2 text-sm text-[#1E293B] before:content-[attr(data-label)] before:font-medium before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="Fecha">{new Date(a.startTime).toLocaleString('es-ES')}</td>
                      <td className="flex flex-col py-2 text-sm md:table-cell md:px-6 md:py-4" data-label="Estado">
                        <span className="inline-block self-end md:self-auto rounded bg-orange-100 px-3 py-1 font-medium text-orange-800">Pendiente</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {/* Table: System Errors with Pagination */}
            {activeTab === 'ERRORS' && (
              <>
                <table className="w-full">
                  <thead className="hidden md:table-header-group bg-gray-50 text-gray-500">
                    <tr>
                      <th className="px-6 py-4 text-left text-sm font-semibold">Fecha</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold">Tipo</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold">Mensaje</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold">Ruta</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold">Acción</th>
                    </tr>
                  </thead>
                  <tbody>
                    {errors.length === 0 ? (
                      <tr><td colSpan={5} className="p-6 text-center text-[#64748B] block md:table-cell">Sistema estable. No hay errores.</td></tr>
                    ) : errors.map(e => (
                      <tr key={e.id} className="block border-b border-[#E2E8F0] bg-white p-4 md:table-row md:p-0 hover:bg-[#F8FAFC]">
                        <td className="flex items-center justify-between py-2 text-sm text-[#1E293B] before:content-[attr(data-label)] before:font-medium before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="Fecha">{new Date(e.timestamp).toLocaleString('es-ES')}</td>
                        <td className="flex flex-col py-2 text-sm text-red-600 font-mono before:content-[attr(data-label)] before:font-medium before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="Tipo">{e.exceptionType}</td>
                        <td className="flex flex-col py-2 text-sm text-[#1E293B] break-words before:content-[attr(data-label)] before:font-medium before:text-[#64748B] before:mb-1 md:table-cell md:px-6 md:py-4 md:before:hidden md:max-w-[250px]" data-label="Mensaje">{e.message}</td>
                        <td className="flex flex-col py-2 text-sm text-gray-400 break-words before:content-[attr(data-label)] before:font-medium before:text-[#64748B] before:mb-1 md:table-cell md:px-6 md:py-4 md:before:hidden md:max-w-[150px]" data-label="Ruta">{e.path}</td>
                        <td className="flex items-center justify-end py-3 md:py-4 md:table-cell md:px-6 border-t md:border-none mt-2 md:mt-0" data-label="Acción">
                          <button onClick={() => dismissError(e.id)} className="w-full md:w-auto rounded bg-[#EF4444] px-4 py-2 text-sm font-medium text-white hover:bg-[#DC2626]">Descartar</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                
                {/* Pagination Controls */}
                {errorTotalPages > 1 && (
                  <div className="flex items-center justify-between border-t border-[#E2E8F0] bg-gray-50 p-4">
                    <button
                      onClick={() => fetchErrorPage(errorPage - 1)}
                      disabled={errorPage === 0}
                      className="rounded border border-[#CBD5E1] bg-white px-4 py-2 text-sm font-medium text-[#475569] transition-colors hover:bg-[#F1F5F9] disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Anterior
                    </button>
                    <span className="text-sm font-medium text-[#64748B]">
                      Página {errorPage + 1} de {errorTotalPages}
                    </span>
                    <button
                      onClick={() => fetchErrorPage(errorPage + 1)}
                      disabled={errorPage >= errorTotalPages - 1}
                      className="rounded border border-[#CBD5E1] bg-white px-4 py-2 text-sm font-medium text-[#475569] transition-colors hover:bg-[#F1F5F9] disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Siguiente
                    </button>
                  </div>
                )}
              </>
            )}
            
          </div>
        )}
      </div>
    </div>
  );
}