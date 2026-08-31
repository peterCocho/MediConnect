export type UserRole = 'ADMIN' | 'DOCTOR' | 'RECEPTIONIST';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  avatar?: string;
}