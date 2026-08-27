import { useState } from 'react';
import type { FormEventHandler } from 'react';
import { Activity, Heart, Pill, Stethoscope } from 'lucide-react';
import { login as loginRequest } from '../../service/api';
import { User } from '../types/user';

interface LoginScreenProps {
  onLogin: (user: User) => void;
}

export function LoginScreen({ onLogin }: LoginScreenProps) {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit: FormEventHandler<HTMLFormElement> = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await loginRequest(username.trim(), password);
      onLogin(response.user);
    } catch (err: any) {
      // Intenta extraer 'mensaje' o 'message' del JSON del backend. 
      // Si no existe, usa el error genérico o un texto por defecto.
      const backendMessage = err.response?.data?.mensaje 
                          || err.response?.data?.message 
                          || 'Credenciales inválidas o error de conexión';
      
      setError(backendMessage);
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <div className="w-full h-screen flex bg-[#F4F7F9]">
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

      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="bg-white p-10 rounded-xl shadow-[0_6px_16px_rgba(0,0,0,0.12)] w-full max-w-md">
          <div className="mb-8 text-center lg:hidden">
            <h1 className="text-[#1E293B] text-3xl font-bold mb-2">MediConnect</h1>
            <p className="text-[#64748B] text-sm">Sistema de Gestión Clínica</p>
          </div>

          <div className="mb-8">
            <h2 className="text-[#1E293B] text-2xl font-bold mb-2">Bienvenido</h2>
            <p className="text-[#64748B] text-sm">Ingrese sus credenciales para acceder al sistema</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-[#334155] mb-2">Usuario</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-3 border border-[#CBD5E1] rounded-lg focus:outline-none focus:border-[#2C7A7B]"
                placeholder="admin"
                autoComplete="username"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#334155] mb-2">Contraseña</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 border border-[#CBD5E1] rounded-lg focus:outline-none focus:border-[#2C7A7B]"
                placeholder="••••••••"
                autoComplete="current-password"
              />
            </div>

            {error ? (
              <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
                {error}
              </div>
            ) : null}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-2 bg-[#2C7A7B] text-white font-medium py-3 rounded-lg hover:bg-[#256d6f] transition-colors disabled:opacity-60"
            >
              {isLoading ? 'Iniciando sesión...' : 'Iniciar sesión'}
            </button>
          </form>

          <div className="mt-6 text-sm text-[#64748B]">
            <p>Credencial por defecto del backend:</p>
            <p className="mt-1 font-medium text-[#1E293B]">Usuario: admin</p>
            <p className="font-medium text-[#1E293B]">Contraseña: admin123</p>
          </div>
        </div>
      </div>
    </div>
  );
}