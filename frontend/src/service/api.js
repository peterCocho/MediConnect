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

const normalizeRole = (roleValue) => {
  const normalized = (roleValue || '').toUpperCase();
  if (normalized === 'ROLE_ADMIN' || normalized === 'ADMIN') return 'ADMIN';
  if (normalized === 'ROLE_DOCTOR' || normalized === 'DOCTOR') return 'DOCTOR';
  if (normalized === 'ROLE_RECEPTION' || normalized === 'ROLE_RECEPTIONIST' || normalized === 'RECEPTIONIST') return 'RECEPTIONIST';
  return null;
};

export const getUserFromToken = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const role = normalizeRole(payload.role);
    if (!payload.sub || !role || (payload.exp && payload.exp < Date.now() / 1000)) return null;

    return {
      id: payload.sub,
      name: payload.sub === 'admin' ? 'Admin User' : payload.sub,
      email: `${payload.sub}@mediconnect.local`,
      role,
    };
  } catch (error) {
    return null;
  }
};

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

  try {
    const user = getUserFromToken(token);
      if (!user) {
        clearSession();
        throw new Error('El token no contiene un rol válido');
      }
    return { user, token };
  } catch (error) {
    clearSession();
    throw error;
  }
};

export const logout = () => {
  clearSession();
};

export default api;