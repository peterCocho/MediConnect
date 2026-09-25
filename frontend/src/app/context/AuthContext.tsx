import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { clearSession, getUserFromToken, login as loginRequest, logout as logoutRequest } from '../../service/api';
import type { User } from '../types/user';

export interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const restoredUser = token ? getUserFromToken(token) : null;

    if (restoredUser) setUser(restoredUser);
    else if (token) clearSession();
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    const response = await loginRequest(username, password);
    setUser(response.user);
  };

  const logout = () => {
    logoutRequest();
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, isLoading, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe utilizarse dentro de AuthProvider');
  return context;
}