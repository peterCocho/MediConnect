// --- CopyButton.tsx ---
import React, { useState, useCallback } from 'react'


interface CopyButtonProps {
  text: string; // Text to copy, for example m.phoneNumber
}

const CopyButton: React.FC<CopyButtonProps> = ({ text }) => {
  const [isCopied, setIsCopied] = useState(false);
  
  /**
   * Copies the text to the clipboard and manages the visual state.
   */
  const handleCopy = useCallback(async () => {
    if (!text) return;

    try {
      await navigator.clipboard.writeText(text);
      setIsCopied(true);
      // Hide the success message after two seconds
      setTimeout(() => setIsCopied(false), 2000);
    } catch (error) {
      console.error('Error al copiar el texto: ', error);
      // An error message could be displayed here if needed
    }
  }, [text]);

  return (
    <button
      onClick={handleCopy}
      className={`flex items-center gap-1 px-2 py-1 text-xs font-medium rounded transition duration-150 ${
        isCopied 
          ? 'bg-green-500 text-white cursor-not-allowed' 
          : 'bg-[#34D399] hover:bg-[#2dd4bf] text-white shadow-md' // Green action button style
      }`}
      disabled={isCopied}
    >
      {/* Icon and text based on the current state */}
      {isCopied ? (
        <>✅ Copiado</>
      ) : (
        <>📋 Copiar</>
      )}
      {/* An optional copy or checkmark icon could be added here using Lucide or Heroicons */}
    </button>
  );
};

export default CopyButton;
