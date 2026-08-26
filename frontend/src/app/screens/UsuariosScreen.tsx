import { Plus } from 'lucide-react';

export function UsuariosScreen() {
  const usuarios = [
    { nombre: 'Dr. Carlos Ramírez', documento: '1234567890', especialidad: 'Cardiología', rol: 'Médico', activo: true },
    { nombre: 'Dra. María López', documento: '0987654321', especialidad: 'Medicina General', rol: 'Médico', activo: true },
    { nombre: 'Dr. José García', documento: '1122334455', especialidad: 'Dermatología', rol: 'Médico', activo: true },
    { nombre: 'Ana Martínez', documento: '5544332211', especialidad: 'N/A', rol: 'Recepcionista', activo: true },
    { nombre: 'Luis Fernández', documento: '6677889900', especialidad: 'N/A', rol: 'Administrador', activo: false },
  ];

  return (
    <div className="p-8 bg-[#F4F7F9] min-h-screen">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-[#1E293B] text-3xl font-bold mb-2">Gestión de Usuarios</h1>
          <p className="text-[#64748B]">Administre el personal y permisos del sistema</p>
        </div>
        <button className="bg-[#2C7A7B] text-white px-6 py-3 rounded-lg font-semibold flex items-center gap-2 hover:bg-[#235E5F] transition-colors">
          <Plus className="w-5 h-5" />
          Nuevo Usuario
        </button>
      </div>

      <div className="bg-white rounded-xl border border-[#CBD5E1] shadow-[0_2px_4px_rgba(0,0,0,0.05)]">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Nombre</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Documento</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Rol</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Especialidad</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-left text-[#64748B] text-xs font-semibold uppercase tracking-wider">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((usuario, index) => (
                <tr key={index} className="border-b border-[#E2E8F0] hover:bg-[#F8FAFC]">
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{usuario.nombre}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{usuario.documento}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{usuario.rol}</td>
                  <td className="px-6 py-4 text-[#1E293B] text-sm">{usuario.especialidad}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <span className={`w-2 h-2 rounded-full ${
                        usuario.activo ? 'bg-[#10B981]' : 'bg-[#94A3B8]'
                      }`} />
                      <span className="text-[#1E293B] text-sm">
                        {usuario.activo ? 'Activo' : 'Inactivo'}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button className="px-4 py-2 bg-[#F1F5F9] text-[#475569] text-sm border border-[#CBD5E1] rounded hover:bg-[#E2E8F0]">
                        Editar
                      </button>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input type="checkbox" checked={usuario.activo} readOnly className="sr-only peer" />
                        <div className={`w-11 h-6 rounded-full peer ${
                          usuario.activo ? 'bg-[#2C7A7B]' : 'bg-[#CBD5E1]'
                        }`}>
                          <div className={`absolute top-[2px] left-[2px] bg-white w-5 h-5 rounded-full transition-transform ${
                            usuario.activo ? 'translate-x-5' : ''
                          }`} />
                        </div>
                      </label>
                    </div>
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