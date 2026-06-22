import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // Ensure paths are correct for your project files
  build: {
    rollupOptions: {
      input: {
        main: './src/main.jsx',
        App: './src/App.jsx',
        Dashboard: './src/pages/Dashboard.jsx',
        DoctorView: './src/pages/DoctorView.jsx',
        ReceptionistView: './src/pages/ReceptionistView.jsx',
      },
    },
  },
});
