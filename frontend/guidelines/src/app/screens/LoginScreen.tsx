import { User, mockUsers } from '../types/user';
import { Stethoscope, Activity, Heart, Pill } from 'lucide-react';

interface LoginScreenProps {
  onLogin: (user: User) => void;
}

export function LoginScreen({ onLogin }: LoginScreenProps) {
  return (
    <div className="w-full h-screen flex bg-[#F4F7F9]">
      {/* Lado izquierdo - Ilustración médica */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-[#2C7A7B] to-[#455A73] p-12 flex-col justify-center items-center relative overflow-hidden">
        <div className="absolute top-10 right-10 opacity-20">
          <Heart className="w-32 h-32 text-white" />
        </div>
        <div className="absolute bottom-20 left-10 opacity-20">
          <Activity className="w-24 h-24 text-white" />
        </div>
        <div className="absolute top-1/3 left-20 opacity-20">
          <Pill className="w-20 h-20 text-white" />
        </div>

        <div className="z-10 text-white text-center">
          <div className="mb-8 flex justify-center">
            <div className="w-24 h-24 bg-[#8CD6D1] rounded-2xl flex items-center justify-center shadow-2xl">
              <Stethoscope className="w-14 h-14 text-[#455A73]" />
            </div>
          </div>
          <h1 className="text-5xl font-bold mb-4">MediConnect</h1>
          <p className="text-xl text-[#8CD6D1] mb-8">Sistema de Gestión Clínica</p>
          <p className="text-lg opacity-90 max-w-md">
            Optimice la atención médica con nuestro sistema integral de historias clínicas y agendamiento automatizado
          </p>
        </div>
      </div>

      {/* Lado derecho - Formulario de login */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="bg-white p-10 rounded-xl shadow-[0_6px_16px_rgba(0,0,0,0.12)] w-full max-w-md">
          <div className="mb-8 text-center lg:hidden">
            <h1 className="text-[#1E293B] text-3xl font-bold mb-2">MediConnect</h1>
            <p className="text-[#64748B] text-sm">Sistema de Gestión Clínica</p>
          </div>

          <div className="mb-8">
            <h2 className="text-[#1E293B] text-2xl font-bold mb-2">Bienvenido</h2>
            <p className="text-[#64748B] text-sm">Seleccione un usuario para iniciar sesión (Demo)</p>
          </div>

          <div className="space-y-3">
            {mockUsers.map((user) => (
              <button
                key={user.id}
                onClick={() => onLogin(user)}
                className="w-full flex items-center gap-4 p-4 bg-[#F8FAFC] border-[1.5px] border-[#94A3B8] rounded-lg hover:border-[#2C7A7B] hover:bg-white transition-all group"
              >
                <div className="w-12 h-12 bg-[#2C7A7B] rounded-full flex items-center justify-center group-hover:scale-110 transition-transform">
                  <span className="text-white font-semibold">
                    {user.name.split(' ').map(n => n[0]).join('')}
                  </span>
                </div>
                <div className="flex-1 text-left">
                  <div className="text-[#1E293B] font-semibold">{user.name}</div>
                  <div className="text-[#64748B] text-sm">
                    {user.role === 'ADMIN' && 'Administrador'}
                    {user.role === 'DOCTOR' && 'Médico'}
                    {user.role === 'RECEPTIONIST' && 'Recepcionista'}
                  </div>
                </div>
                <div className="text-[#2C7A7B] opacity-0 group-hover:opacity-100 transition-opacity">
                  →
                </div>
              </button>
            ))}
          </div>

          <div className="mt-8 text-center">
            <a href="#" className="text-[#64748B] text-sm hover:text-[#2C7A7B]">
              ¿Problemas para ingresar? Contacte al administrador
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}