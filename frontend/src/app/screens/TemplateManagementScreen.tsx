import { Loader2, Plus, Save, Pencil, Power, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../../service/api';

type Template = {
  id: number;
  name: string;
  description: string;
  templateContent: string;
  isActive: boolean;
};

export function TemplateManagementScreen() {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // State for form and editing mode
  const [form, setForm] = useState({ name: '', description: '', template: '' });
  const [editingId, setEditingId] = useState<number | null>(null);

  const loadTemplates = async () => {
    try {
      setIsLoading(true);
      const response = await api.get('/api/templates');
      setTemplates(response.data ?? []);
      setError('');
    } catch (err) {
      setError('No se pudieron cargar las plantillas del sistema.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTemplates();
  }, []);

  // Populate form when editing a template
  const handleEdit = (template: Template) => {
    setEditingId(template.id);
    setForm({
      name: template.name,
      description: template.description || '',
      template: template.templateContent || '',
    });
    setError('');
  };

  // Reset form to creation mode
  const handleCancelEdit = () => {
    setEditingId(null);
    setForm({ name: '', description: '', template: '' });
    setError('');
  };

  // Toggle template active status
  const handleToggleStatus = async (id: number, currentStatus: boolean) => {
    try {
      setError('');
      if (currentStatus) {
        await api.patch(`/api/templates/${id}/deactivate`);
      } else {
        await api.patch(`/api/templates/${id}/activate`);
      }
      await loadTemplates();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al cambiar el estado de la plantilla.');
    }
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmedName = form.name.trim();
    const trimmedTemplate = form.template.trim();

    if (!trimmedName || !trimmedTemplate) {
      setError('Nombre y contenido de la plantilla son obligatorios.');
      return;
    }

    try {
      setIsSubmitting(true);
      setError('');
      
      const payload = {
        name: trimmedName,
        description: form.description.trim(),
        template: trimmedTemplate, 
      };

      if (editingId) {
        // Update existing template
        await api.put(`/api/templates/${editingId}`, payload);
      } else {
        // Create new template
        await api.post('/api/templates', payload);
      }
      
      handleCancelEdit(); // Reset form
      await loadTemplates();
    } catch (err: any) {
      setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo guardar la plantilla.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F4F7F9] p-4 sm:p-6 lg:p-8">
      <div className="mb-8 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="mb-2 text-2xl font-bold text-[#1E293B] sm:text-3xl">Plantillas Clínicas</h1>
          <p className="text-sm text-[#64748B] sm:text-base">Gestione las estructuras base para notas médicas</p>
        </div>
        <button 
          onClick={handleCancelEdit}
          className="inline-flex items-center gap-2 rounded-lg bg-[#2C7A7B] px-4 py-3 font-semibold text-white transition-colors hover:bg-[#235E5F]"
        >
          <Plus className="h-4 w-4" />
          Nueva Plantilla
        </button>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="rounded-xl border border-[#CBD5E1] bg-white shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
          {isLoading ? (
            <div className="flex items-center justify-center gap-3 py-12 text-[#64748B]">
              <Loader2 className="h-5 w-5 animate-spin" />
              <span>Cargando plantillas...</span>
            </div>
          ) : error && templates.length === 0 ? (
            <div className="px-6 py-8 text-center text-red-600">{error}</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="hidden md:table-header-group">
                  <tr className="border-b border-[#E2E8F0] bg-[#F8FAFC]">
                    <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Nombre</th>
                    <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Descripción</th>
                    <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Estado</th>
                    <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-[#64748B] sm:px-6">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {templates.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-10 text-center text-[#64748B]">No hay plantillas registradas.</td>
                    </tr>
                  ) : (
                    templates.map((template) => (
                      <tr key={template.id} className="mb-4 block border-b border-[#E2E8F0] bg-white p-4 shadow-sm hover:bg-[#F8FAFC] md:mb-0 md:table-row md:p-0 md:shadow-none">
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm font-medium text-[#1E293B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Nombre">{template.name}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right text-sm text-[#64748B] break-words whitespace-normal before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Descripción">{template.description || 'Sin descripción'}</td>
                        <td className="flex items-center justify-between gap-3 px-0 py-2 text-right before:mr-2 before:text-[#64748B] before:content-[attr(data-label)] md:table-cell md:px-6 md:py-4 md:text-left md:before:hidden" data-label="Estado">
                          <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${template.isActive ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                            {template.isActive ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td className="flex justify-end gap-2 px-0 py-2 before:content-[attr(data-label)] before:mr-auto before:text-[#64748B] md:table-cell md:px-6 md:py-4 md:before:hidden" data-label="Acciones">
                          <div className="flex justify-end gap-2">
                            <button
                              onClick={() => handleEdit(template)}
                              className="rounded p-1.5 text-blue-600 hover:bg-blue-50 transition-colors"
                              title="Editar plantilla"
                            >
                              <Pencil className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleToggleStatus(template.id, template.isActive)}
                              className={`rounded p-1.5 transition-colors ${template.isActive ? 'text-red-600 hover:bg-red-50' : 'text-emerald-600 hover:bg-emerald-50'}`}
                              title={template.isActive ? 'Desactivar plantilla' : 'Activar plantilla'}
                            >
                              <Power className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="self-start rounded-xl border border-[#CBD5E1] bg-white p-5 shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-bold text-[#1E293B]">
              {editingId ? 'Editar plantilla' : 'Nueva plantilla'}
            </h2>
            {editingId && (
              <button onClick={handleCancelEdit} className="text-[#64748B] hover:text-[#1E293B]" title="Cancelar edición">
                <X className="h-5 w-5" />
              </button>
            )}
          </div>
          
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="mb-2 block text-sm font-medium text-[#334155]">Nombre</label>
              <input
                value={form.name}
                onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
                className="w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
                placeholder="Ej: SOAP General"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#334155]">Descripción</label>
              <input
                value={form.description}
                onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
                className="w-full rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
                placeholder="Breve descripción"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#334155]">Contenido</label>
              <textarea
                value={form.template}
                onChange={(event) => setForm((current) => ({ ...current, template: event.target.value }))}
                rows={7}
                className="w-full resize-none rounded-md border border-[#CBD5E1] bg-[#F8FAFC] px-3 py-2 text-[#1E293B] focus:border-[#2C7A7B] focus:outline-none"
                placeholder="Motivo:&#10;Examen físico:&#10;Plan:"
              />
            </div>

            {error && templates.length > 0 ? (
              <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">{error}</div>
            ) : null}

            <div className="flex gap-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-[#2C7A7B] px-4 py-3 font-semibold text-white transition-colors hover:bg-[#235E5F] disabled:opacity-70"
              >
                {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                {isSubmitting ? 'Guardando...' : editingId ? 'Actualizar' : 'Guardar'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}