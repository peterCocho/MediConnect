// --- CopyButton.tsx ---
import React, { useState, useCallback } from 'react'


interface CopyButtonProps {
  text: string; // El texto que se debe copiar (ej: m.phoneNumber)
}

const CopyButton: React.FC<CopyButtonProps> = ({ text }) => {
  const [isCopied, setIsCopied] = useState(false);
  
  /**
   * Función para copiar el texto al portapapeles y gestionar el estado visual.
   */
  const handleCopy = useCallback(async () => {
    if (!text) return;

    try {
      await navigator.clipboard.writeText(text);
      setIsCopied(true);
      // Desactivar el mensaje de éxito después de 2 segundos
      setTimeout(() => setIsCopied(false), 2000);
    } catch (error) {
      console.error('Error al copiar el texto: ', error);
      // Opcionalmente, podrías mostrar un mensaje de error aquí
    }
  }, [text]);

  return (
    <button
      onClick={handleCopy}
      className={`flex items-center gap-1 px-2 py-1 text-xs font-medium rounded transition duration-150 ${
        isCopied 
          ? 'bg-green-500 text-white cursor-not-allowed' 
          : 'bg-[#34D399] hover:bg-[#2dd4bf] text-white shadow-md' // Verde para el botón de acción
      }`}
      disabled={isCopied}
    >
      {/* Icono y texto basados en el estado */}
      {isCopied ? (
        <>✅ Copiado</>
      ) : (
        <>📋 Copiar</>
      )}
      {/* Opcional: Añadir un icono de copia o checkmark aquí usando librerías como Lucide/Heroicons */}
    </button>
  );
};

export default CopyButton;
