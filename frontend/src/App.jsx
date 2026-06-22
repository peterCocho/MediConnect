import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext'; // Contexto que maneja el estado del usuario

// Componentes de las vistas (Dashboard, Appointments, Patients)
import Dashboard from './pages/Dashboard';
import DoctorView from './pages/DoctorView';
import ReceptionistView from './pages/ReceptionistView';

const ProtectedRoute = ({ element, allowedRoles }) => {
    const { user, isLoading } = useAuth();

    if (isLoading) return <div>Cargando...</div>;

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Verificar si el rol del usuario está en la lista de roles permitidos
  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />; // Redirigir a una página de error o inicio
  }

    return element;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<ProtectedRoute element={<Dashboard />} allowedRoles={['ADMIN', 'DOCTOR', 'RECEPTIONIST']} />} />
        <Route path="/doctor/*" element={<ProtectedRoute element={<DoctorView />} allowedRoles={['DOCTOR']} />} />
        <Route path="/receptionist/*" element={<ProtectedRoute element={<ReceptionistView />} allowedRoles={['RECEPTIONIST']} />} />
      </Routes>
    </Router>
  );
}

export default App;

function Login() {
  // Implement login logic here
  return (
    <div>
      <h1>Login</h1>
      <input type="text" placeholder="Username" />
      <input type="password" placeholder="Password" />
      <button>Submit</button>
    </div>
  );
}
