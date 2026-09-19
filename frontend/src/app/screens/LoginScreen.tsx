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
    <div className="relative min-h-screen w-full bg-[#F4F7F9]">

      {/* ===== FONDO: Animación (cubre toda la pantalla) ===== */}
      <div className="absolute inset-0 z-0">
        <AnimatedGradient config={{ preset: "Oceanic" }} />
        <div className="absolute inset-0 bg-[#001d3d]/20 mix-blend-multiply" />
      </div>

      {/* ===== CONTENIDO: Columna en móvil/tablet, fila en desktop ===== */}
      <div className="relative z-10 flex min-h-screen w-full flex-col lg:flex-row">

        {/* ----- Bloque Logo + Nombre (altura natural) ----- */}
        <div className="flex w-full flex-col items-center justify-center px-6 pt-8 pb-6 text-center text-white sm:pt-10 md:pt-12 lg:w-1/2 lg:py-0 lg:px-8">
          <div className="mb-3 flex justify-center sm:mb-4 md:mb-6 lg:mb-8">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl border border-white/20 bg-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.2)] backdrop-blur-md sm:h-20 sm:w-20 md:h-24 md:w-24 lg:h-24 lg:w-24">
              <MediConnectBrandIcon className="h-10 w-10 text-white sm:h-12 sm:w-12 md:h-14 md:w-14 lg:h-14 lg:w-14" />
            </div>
          </div>
          <h1 className="text-3xl font-bold drop-shadow-lg sm:text-4xl md:text-5xl lg:text-5xl">
            MediConnect
          </h1>
          <p className="mt-2 text-sm font-light tracking-wide text-white/90 sm:text-base md:text-lg lg:mt-4 lg:text-xl">
            Sistema de Gestión Clínica Automatizada
          </p>
        </div>

        {/* ----- Bloque Tarjeta de Login (ocupa el resto y centra) ----- */}
        <div className="flex w-full flex-1 items-center justify-center px-4 pb-8 sm:px-6 md:px-10 lg:w-1/2 lg:p-8">
          <div className="w-full max-w-md rounded-xl bg-white p-5 shadow-[0_6px_16px_rgba(0,0,0,0.12)] sm:p-7 md:max-w-lg md:p-8 lg:max-w-md lg:p-10">

            <div className="mb-6 text-center md:mb-8">
              <h2 className="mb-2 text-2xl font-bold text-[#1E293B] md:text-3xl">
                Bienvenido
              </h2>
              <p className="text-sm text-[#64748B] md:text-base">
                Ingrese sus credenciales para acceder al sistema
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4 md:space-y-5">
              <div>
                <label className="mb-2 block text-sm font-medium text-[#334155] md:text-base">
                  Usuario
                </label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className={`w-full rounded-lg border px-4 py-3 text-base transition-colors focus:outline-none md:py-3.5 ${
                    error
                      ? 'border-red-500 bg-red-50 focus:border-red-500'
                      : 'border-slate-300 bg-slate-50 focus:border-[#2C7A7B]'
                  }`}
                  placeholder="admin"
                  autoComplete="username"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-[#334155] md:text-base">
                  Contraseña
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={`w-full rounded-lg border px-4 py-3 text-base transition-colors focus:outline-none md:py-3.5 ${
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
                className="mt-2 w-full rounded-lg bg-[#2C7A7B] py-3 text-base font-medium text-white transition-colors hover:bg-[#256d6f] disabled:opacity-60 md:py-3.5"
              >
                {isLoading ? 'Iniciando sesión...' : 'Iniciar sesión'}
              </button>
            </form>

            <div className="mt-5 text-sm text-[#64748B] md:mt-6 md:text-base">
              <p>Credencial por defecto del backend:</p>
              <p className="mt-1 font-medium text-[#1E293B]">Usuario: admin</p>
              <p className="font-medium text-[#1E293B]">Contraseña: admin123</p>
            </div>

          </div>
        </div>

      </div>
    </div>
  );
}