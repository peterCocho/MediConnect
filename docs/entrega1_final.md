# Entrega 1 — Análisis y Planeación Final — MediConnect

**Aprendiz:** PEDRO PABLO CONTRERAS VEGA

**Instructor:** EDGAR CARVAJAL CACERES

**Programa:** Programación de Software (Ficha: 3186277)

**Año:** 2026

---

## Resumen ejecutivo
Este documento es la versión finalizada para la Entrega 1 del proyecto MediConnect. Contiene el alcance, historias de usuario, contratos API, requisitos no funcionales, arquitectura propuesta, cronograma de las 6 entregas, plan de pruebas y evidencia (diagrams referenciados). Las imágenes se mantienen en su ubicación original.

## Objetivo
Consolidar los artefactos que demuestran el análisis funcional y técnico del MVP: documentación, diagramas, estructura del repositorio y checklist de entrega.

## Alcance del MVP (resumen)
- Autenticación y RBAC (Admin / Médico / Recepción)
- Motor de agendamiento con control de concurrencia (bloques 30 min, PESSIMISTIC_WRITE)
- Gestión de Pacientes y Expedientes (CRUD, cifrado de datos sensibles)
- Notificaciones asíncronas (n8n → WhatsApp)
- Documentación API (OpenAPI/Swagger)

## Tecnologías y herramientas
- Backend: Java, Spring Boot, Spring Security, Spring Data JPA, Flyway
- Frontend: React, Vite/CRA, Nginx para despliegue estático
- DB: PostgreSQL (migrations con Flyway)
- Integración asíncrona: n8n
- Encriptación/seguridad: Argon2id (passwords), campo-level encryption (AES-GCM con KMS)
- CI/CD: GitHub Actions / GitLab CI (opcional)

## Historias de usuario (resumen)
(Se conservan y refinan las HU ya listadas en el análisis original; ver sección detallada en el documento principal.)

## Contratos API críticos
- POST /api/consultations — Agendar cita (Recepcionista)
  - Request: { medicalRecordId, doctorId, consultationDate (ISO-8601) }
  - Responses: 201, 400, 403, 409

- POST /api/webhooks/n8n/status — Callback n8n (sin JWT; valida x-api-key)
  - Request DTO: { notification_id: Long, status: 'SENT'|'FAILED', provider_id?: String }

## Requisitos no funcionales (esenciales)
- JWT TTL: 2 horas (sin refresh token)
- Latencias objetivo: GET <300ms, escritura compleja <800ms
- CORS: restringido al dominio de frontend
- Concurrencia: PESSIMISTIC_WRITE a nivel de fila en agendamiento
- Auditoría y soft delete implementados en entidades críticas

## Arquitectura y despliegue (resumen)
- Contenedores Docker para backend, frontend, DB y n8n
- Single-tenant: una instancia por clínica
- Backups: pg_dump encriptado y offsite (RTO objetivo < 4h)

## Cronograma (6 meses — entregas mensuales)
- Mes 1: Análisis, estructura del repo, ER, contratos API, prototipo auth
- Mes 2: Motor de agendamiento (transaccional) + pruebas de concurrencia
- Mes 3: CRUD pacientes/expedientes + cifrado campo-level
- Mes 4: Integración n8n + notificaciones + webhooks (callback)
- Mes 5: Frontend completo (pantallas médicas, agenda, gestión de usuarios)
- Mes 6: QA, hardening, documentación final y entrega

## Plan de pruebas (resumen)
- Unit tests: cobertura mínima 65% en servicios críticos
- Integration tests: agendamiento concurrente y endpoints de notificación
- Pruebas de carga: validar comportamiento bajo concurrencia para agendamiento
- Pruebas de seguridad: verificación de cifrado, control de accesos y CORS

## Criterios de aceptación de la entrega 1
- Documento MD completo y legible (este archivo y analisis-planeacion.md)
- Diagramas ER y de flujo incluidos en docs/diagramas/
- Estructura de carpetas presente en repo
- Checklist de entrega completado (deliveries_mes_01_checklist.md)

## Instrucciones para revisión de imágenes (no tocar archivos)
- Las imágenes se encuentran en: docs/diagramas/
- Abrir `docs/entrega1_final.md` o `docs/analisis-planeacion.md` desde VS Code y usar el preview. Si las imágenes no se muestran al abrir el MD fuera del repositorio, abrir el MD directamente desde la carpeta del proyecto para que las rutas relativas resuelvan.

## Ubicación de artefactos importantes
- docs/analisis-planeacion.md (versión de trabajo) — revisar
- docs/entrega1_final.md (este archivo) — versión definitiva para entrega 1
- docs/diagramas/ (imágenes y diagramas)
- Analisis-Planeacion-Mediconnect/ (originales importados)
- scripts/ (scripts SQL y utilitarios)

---

Si quieres, pego aquí los párrafos faltantes concretos que notes o adapto el texto para cumplir el formato SENA (portada oficial). ¿Deseas que lo formatee al estilo plantilla SENA y añada metadatos de la ficha y evidencias?