# Plan: Refactorizar Pantallas MediConnect

## Resumen Ejecutivo

Este plan documenta las correcciones necesarias para sincronizar el frontend con los DTOs del backend de Spring Boot y resolver problemas de diseño responsivo.

## Hallazgo Crítico: "Boolean Trap" Requerido en Backend

**Problema identificado:** El backend serializa `isActive` (Lombok `@Data` + `boolean isActive` → getter `isActive()` → Jackson emite `isActive`). El frontend usa `usuario.active ?? usuario.isActive ?? false`, lo cual funciona como safety net. **Sin embargo**, el `mapToDoctorResponse` en `UserServiceImpl.java` NO incluye `.isActive()`, por lo que todos los doctores siempre aparecen como inactivos.

## Tareas de Implementación

### Tarea 1: Backend Fix - mapToDoctorResponse (Requerido)

**Archivo:** `backend/src/main/java/com/sena/backend/service/impl/UserServiceImpl.java:138-148`

**Cambio requerido:**
```java
private DoctorResponse mapToDoctorResponse(Doctor doctor) {
    return DoctorResponse.builder()
            .id(doctor.getId())
            .documentNumber(doctor.getDocumentNumber())
            .fullName(doctor.getFullName())
            .email(doctor.getEmail())
            .phone(doctor.getPhone())
            .specialty(doctor.getSpecialty())
            .username(doctor.getUser().getUsername())
            .isActive(doctor.getUser().getIsActive())  // <-- FALTABA
            .build();
}
```

### Tarea 2: Frontend Cleanup - mockUsers (Requerido)

**Archivo:** `frontend/src/app/types/user.ts:11-30`

**Cambio requerido:** Eliminar el array `mockUsers` ya que no se usa en ningún lugar y viola la regla de "no mock data".

```typescript
// ELIMINAR estas líneas 11-30:
export const mockUsers: User[] = [
  { id: '1', name: 'Admin User', email: 'admin@mediconnect.com', role: 'ADMIN' },
  { id: '2', name: 'Dr. Carlos Ramírez', email: 'carlos.ramirez@mediconnect.com', role: 'DOCTOR' },
  { id: '3', name: 'Ana Martínez', email: 'ana.martinez@mediconnect.com', role: 'RECEPTIONIST' },
];
```

### Tarea 3: Responsive Sidebar (Requerido)

**Archivo:** `frontend/src/app/components/Sidebar.tsx:154`

**Estado actual:** El Sidebar no usa los props `isOpen`/`onClose` y no tiene las clases responsivas de transform.

**Cambio requerido:**
```typescript
// En la función Sidebar, agregar destructuring de isOpen y onClose
export function Sidebar({
  activeScreen,
  onNavigate,
  userRole,
  isOpen = false,  // Añadir
  onClose,         // Añadir
}: SidebarProps) {

  // En el aside, cambiar la clase:
  <aside className={`fixed inset-y-0 left-0 z-50 w-64 transform transition-transform duration-300 md:relative md:translate-x-0 ${isOpen ? 'translate-x-0' : '-translate-x-full'} bg-[#455A73] text-white md:flex md:flex-col md:transform-none`}>
```

**Archivo:** `frontend/src/app/components/Sidebar.tsx:186-190`

**Cambio adicional:** Eliminar línea 186 `border-t` y ajustar el padding para que funcione bien con el ancho fijo en mobile.

### Tarea 4: Header Ajustes (Opcional pero Recomendado)

**Archivo:** `frontend/src/app/components/Header.tsx:17`

**Cambio sugerido:** Ajustar el z-index del header para que esté por encima del sidebar en móvil:
```typescript
<header className="relative z-40 border-b border-[#E2E8F0] bg-white px-4 py-3 sm:px-6 lg:px-8 lg:py-4">
```

## ArchivosYa Implementados (Verificar)

Los siguientes cambios YA están implementados correctamente:

1. **Boolean Trap en Frontend** ✓
   - `UsersScreen.tsx:13-14,194` - `isActive?: boolean; active?: boolean;` y `const isActive = usuario.active ?? usuario.isActive ?? false;`
   - `ReceptionistsScreen.tsx:11-12,177` - mismo patrón
   - `PatientsScreen.tsx:11-12,116` - mismo patrón

2. **Table to Cards** ✓
   - Todas las pantallas con tablas ya usan el patrón `hidden md:table-header-group`, `block border-b mb-4 p-4 shadow-sm md:table-row md:mb-0 md:p-0 md:shadow-none`, y `flex justify-between items-center text-right md:table-cell md:text-left`

3. **AgendaGlobalScreen** ✓
   - Ya renderiza `Paciente ID: ${cita.patientId}` y `Médico ID: ${cita.doctorId}`
   - Ya usa `toLocaleTimeString` y `toLocaleDateString`

4. **Dashboard con 5 citas** ✓
   - `DashboardScreen.tsx:37` usa `params: { page: 0, size: 5, sort: 'startTime,desc' }`

5. **Sidebar props** ✓
   - `Sidebar.tsx:46-47` ya tiene `isOpen?: boolean; onClose?: () => void;`
   - `App.tsx:53-62` ya pasa `isOpen` y `onClose` al Sidebar

## Endpoint Coverage Verificado

| Pantalla | Endpoint(s) | Estado |
|----------|-------------|--------|
| DashboardScreen | `/api/appointments`, `/api/reports/dashboard` | ✓ |
| AgendaGlobalScreen | `/api/appointments` | ✓ |
| UsersScreen | `/api/users/doctors` | ✓ |
| ReceptionistsScreen | `/api/receptionists` | ✓ |
| PatientsScreen | `/api/patients` | ✓ |
| AgendamientoScreen | `/api/patients`, `/api/users/doctors`, `/api/appointments/book` | ✓ |
| NotificationsScreen | `/api/whatsapp/messages/unread` | ✓ |
| MyScheduleScreen | `/api/consultations` | ✓ |
| MyPatientsScreen | `/api/patients`, `/api/consultations` | ✓ |
| MedicalHistoryScreen | `/api/consultations/patient-timeline/{id}` | ✓ |
| ConsultationScreen | `/api/consultations/{id}`, `/api/consultations/{id}/execute` | ✓ |
| ReportsScreen | `/api/reports/dashboard` | ✓ |
| AdministracionPlantillasScreen | `/api/templates` | ✓ |
| PatientRegistrationScreen | `/api/patients` | ✓ |

## Orden de Implementación Sugerida

1. **Backend:** Corregir `mapToDoctorResponse` en `UserServiceImpl.java` (Tarea 1)
2. **Frontend:** Eliminar `mockUsers` de `user.ts` (Tarea 2)
3. **Frontend:** Actualizar `Sidebar.tsx` con clases responsivas (Tarea 3)
4. **Frontend:** Ajustar z-index del Header (Tarea 4)

## Verificación Post-Implementación

1. Verificar que doctores aparecen como "Activos" cuando `isActive=true` en BD
2. Verificar menú hamburguesa en móvil abre/cierra el sidebar
3. Verificar sidebar se muestra siempre expandido en desktop (md+)
4. Verificar que no hay datos mock en las pantallas
5. Verificar table-to-cards funciona en pantallas pequeñas
