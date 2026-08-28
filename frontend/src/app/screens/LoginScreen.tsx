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
    <div className="w-full h-screen flex bg-[#F4F7F9]">
      
      {/* Sección Izquierda: Fondo animado y Branding */}
      <div className="hidden lg:flex lg:w-1/2 relative overflow-hidden flex-col justify-center items-center">
        
        {/* Capa Base: Componente WebGL Animado */}
        <div className="absolute inset-0 z-0">
          <AnimatedGradient config={{ preset: "Oceanic" }} />
        </div>

        {/* Capa Filtro: Oscurece ligeramente el fondo para que el texto blanco sea legible */}
        <div className="absolute inset-0 bg-[#001d3d]/20 z-10 mix-blend-multiply" />

        {/* Capa Superior: Texto e Isotipo */}
        <div className="z-20 text-white text-center px-8 w-full">
          <div className="mb-8 flex justify-center">
            {/* Efecto Glassmorphism */}
            <div className="w-24 h-24 bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl flex items-center justify-center shadow-[0_8px_32px_rgba(0,0,0,0.2)]">
              <MediConnectBrandIcon className="w-14 h-14 text-white" />
            </div>
          </div>
          <h1 className="text-5xl font-bold mb-4 drop-shadow-lg">MediConnect</h1>
          <p className="text-xl text-white/90 mb-8 font-light tracking-wide drop-shadow-md">
            Sistema de Gestión Clínica Automatizada
          </p>
        </div>
      </div>

      {/* Sección Derecha: Formulario de Login (Sin cambios estructurales) */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="bg-white p-10 rounded-xl shadow-[0_6px_16px_rgba(0,0,0,0.12)] w-full max-w-md">
          <div className="mb-8 text-center lg:hidden">
            <h1 className="text-[#1E293B] text-3xl font-bold mb-2">MediConnect</h1>
            <p className="text-[#64748B] text-sm">Sistema de Gestión Clínica Automatizada</p>
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
      // Dynamic classes applied for error state and better default visibility
      className={`w-full px-4 py-3 border rounded-lg focus:outline-none transition-colors ${
        error 
          ? 'border-red-500 bg-red-50 focus:border-red-500' 
          : 'border-slate-300 bg-slate-50 focus:border-[#2C7A7B]'
      }`}
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
      // Dynamic classes applied for error state and better default visibility
      className={`w-full px-4 py-3 border rounded-lg focus:outline-none transition-colors ${
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