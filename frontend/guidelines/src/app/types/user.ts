export type UserRole = 'ADMIN' | 'DOCTOR' | 'RECEPTIONIST';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  avatar?: string;
}

export const mockUsers: User[] = [
  {
    id: '1',
    name: 'Admin User',
    email: 'admin@mediconnect.com',
    role: 'ADMIN',
  },
  {
    id: '2',
    name: 'Dr. Carlos Ramírez',
    email: 'carlos.ramirez@mediconnect.com',
    role: 'DOCTOR',
  },
  {
    id: '3',
    name: 'Ana Martínez',
    email: 'ana.martinez@mediconnect.com',
    role: 'RECEPTIONIST',
  },
];