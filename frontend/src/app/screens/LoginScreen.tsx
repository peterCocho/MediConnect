import { useState } from 'react';
import { AnimatedGradient } from '../components/ui/animated-gradient';
import type { FormEventHandler } from 'react';
import { useNavigate } from 'react-router';
import { useAuth } from '../context/AuthContext';

// 1. Isotipo personalizado que reemplaza al estetoscopio
const MediConnectBrandIcon = ({ className = "w-14 h-14 text-white" }) => (
  <svg 
    className={className} 
    viewBox="0 0 24 24" 
    fill="none" 
    xmlns="http://www.w3.org/2000/svg"
  >
    <path 
      d="M4 19V7C4 5.34315 5.34315 4 7 4H8.5L12 10L15.5 4H17C18.6569 4 20 5.34315 20 7V19" 
      stroke="currentColor" 
      strokeWidth="2.5" 
      strokeLinecap="round" 
      strokeLinejoin="round"
    />
    <circle cx="12" cy="10" r="3" fill="currentColor" />
    <path d="M12 13V20" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
  </svg>
);

export function LoginScreen() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit: FormEventHandler<HTMLFormElement> = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(username.trim(), password);
      navigate('/dashboard', { replace: true });
    } catch (err: any) {
      const backendMessage = err.response?.data?.mensaje 
                          || err.response?.data?.message 
                          || 'Credenciales inválidas o error de conexión';
      
      setError(backendMessage);
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <div className="flex min-h-screen w-full bg-[#F4F7F9]">
      <div className="relative hidden w-1/2 overflow-hidden lg:flex lg:flex-col lg:items-center lg:justify-center">
        <div className="absolute inset-0 z-0">
          <AnimatedGradient config={{ preset: "Oceanic" }} />
        </div>
        <div className="absolute inset-0 z-10 bg-[#001d3d]/20 mix-blend-multiply" />

        <div className="z-20 w-full px-6 text-center text-white sm:px-8">
          <div className="mb-8 flex justify-center">
            <div className="flex h-20 w-20 items-center justify-center rounded-2xl border border-white/20 bg-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.2)] backdrop-blur-md sm:h-24 sm:w-24">
              <MediConnectBrandIcon className="h-12 w-12 text-white sm:h-14 sm:w-14" />
            </div>
          </div>
          <h1 className="mb-4 text-4xl font-bold drop-shadow-lg sm:text-5xl">MediConnect</h1>
          <p className="mb-8 text-base font-light tracking-wide text-white/90 sm:text-xl">
            Sistema de Gestión Clínica Automatizada
          </p>
        </div>
      </div>

      <div className="flex w-full items-center justify-center p-4 sm:p-6 lg:w-1/2 lg:p-8">
        <div className="w-full max-w-md rounded-xl bg-white p-5 shadow-[0_6px_16px_rgba(0,0,0,0.12)] sm:p-8 lg:p-10">
          <div className="mb-8 text-center lg:hidden">
            <h1 className="mb-2 text-3xl font-bold text-[#1E293B]">MediConnect</h1>
            <p className="text-sm text-[#64748B]">Sistema de Gestión Clínica Automatizada</p>
          </div>

          <div className="mb-8">
            <h2 className="mb-2 text-2xl font-bold text-[#1E293B]">Bienvenido</h2>
            <p className="text-sm text-[#64748B]">Ingrese sus credenciales para acceder al sistema</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="mb-2 block text-sm font-medium text-[#334155]">Usuario</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className={`w-full rounded-lg border px-4 py-3 transition-colors focus:outline-none ${
                  error
                    ? 'border-red-500 bg-red-50 focus:border-red-500'
                    : 'border-slate-300 bg-slate-50 focus:border-[#2C7A7B]'
                }`}
                placeholder="admin"
                autoComplete="username"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-[#334155]">Contraseña</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className={`w-full rounded-lg border px-4 py-3 transition-colors focus:outline-none ${
                  error
                    ? 'border-red-500 bg-red-50 focus:border-red-500'
                    : 'border-slate-300 bg-slate-50 focus:border-[#2C7A7B]'
                }`}
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
              className="mt-2 w-full rounded-lg bg-[#2C7A7B] py-3 font-medium text-white transition-colors hover:bg-[#256d6f] disabled:opacity-60"
            >
              {isLoading ? 'Iniciando sesión...' : 'Iniciando sesión'}
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