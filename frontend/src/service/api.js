import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const isTokenExpired = (token) => {
  if (!token) return true;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const currentTime = Date.now() / 1000;
    return payload.exp < currentTime;
  } catch (error) {
    return true;
  }
};

const getToken = () => localStorage.getItem('token');

export const clearSession = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('isLoggedIn');
};

api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token && !isTokenExpired(token)) {
      config.headers.Authorization = `Bearer ${token}`;
    } else if (token && isTokenExpired(token)) {
      clearSession();
      return Promise.reject(new Error('La sesión ha expirado'));
    }
    return config;
  },
  (error) => Promise.reject(error),
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      clearSession();
    }
    return Promise.reject(error);
  },
);

/**
 * @typedef {{
 *   user: import('../app/types/user').User,
 *   token: string
 * }} LoginResponse
 */

/**
 * @param {string} username
 * @param {string} password
 * @returns {Promise<LoginResponse>}
 */
export const login = async (username, password) => {
  const response = await api.post('/api/auth/login', { username, password });
  const token = response.data?.token;

  if (!token) {
    throw new Error('No se recibió el token de acceso desde el servidor');
  }

  localStorage.setItem('token', token);
  localStorage.setItem('userEmail', `${username}@mediconnect.local`);
  localStorage.setItem('isLoggedIn', 'true');

  // Normalize role to ensure UI consistency regardless of backend format variations
  const normalizeRole = (roleValue) => {
    const normalized = (roleValue || '').toUpperCase();
    if (normalized === 'ROLE_ADMIN' || normalized === 'ADMIN') return 'ADMIN';
    if (normalized === 'ROLE_DOCTOR' || normalized === 'DOCTOR') return 'DOCTOR';
    if (normalized === 'ROLE_RECEPTION' || normalized === 'ROLE_RECEPTIONIST' || normalized === 'RECEPTIONIST') return 'RECEPTIONIST';
    return 'DOCTOR'; // Default fallback
  };

  try {
    // Decode JWT payload and extract embedded claims
    const payload = JSON.parse(atob(token.split('.')[1]));
    const normalizedUsername = payload.sub || username;
    
    // Read the role directly from the payload injected by the backend
    const mappedRole = normalizeRole(payload.role);

    return {
      user: {
        id: normalizedUsername,
        name: normalizedUsername === 'admin' ? 'Admin User' : normalizedUsername,
        email: `${normalizedUsername}@mediconnect.local`,
        role: mappedRole,
      },
      token,
    };
  } catch (error) {
    // Fallback if token parsing fails to prevent full application crash
    return {
      user: {
        id: username,
        name: username,
        email: `${username}@mediconnect.local`,
        role: 'DOCTOR',
      },
      token,
    };
  }
};

export const logout = () => {
  clearSession();
};

export default api;