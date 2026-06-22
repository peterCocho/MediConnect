# MediConnect Agent Instructions

## Overview
- This is a full-stack application with a Spring Boot backend in [backend](backend) and a Vite/React frontend in [frontend](frontend).
- Prefer small, local changes that respect the existing layering and implementation style.
- Use the project docs for broader context instead of repeating their content here: [docs/entrega1_final.md](docs/entrega1_final.md), [docs/analisis-planeacion.md](docs/analisis-planeacion.md), [frontend/README.md](frontend/README.md), [Analisis-Planeacion-Mediconnect/estructura-carpetas.md](Analisis-Planeacion-Mediconnect/estructura-carpetas.md), [Analisis-Planeacion-Mediconnect/deliveries_mes_01_checklist.md](Analisis-Planeacion-Mediconnect/deliveries_mes_01_checklist.md), and [Analisis-Planeacion-Mediconnect/README_export_instructions.md](Analisis-Planeacion-Mediconnect/README_export_instructions.md).

## Commands
- Backend root: use the Maven wrapper from [backend/mvnw.cmd](backend/mvnw.cmd) on Windows or [backend/mvnw](backend/mvnw) on Unix-like systems.
- Common backend commands: `mvnw.cmd test`, `mvnw.cmd spring-boot:run`, and `mvnw.cmd package` from the [backend](backend) folder.
- Frontend commands live in [frontend/package.json](frontend/package.json): `pnpm install`, `pnpm run dev`, `pnpm run build`, `pnpm run lint`, and `pnpm run preview` from the [frontend](frontend) folder.
- Validate focused changes with the narrowest relevant command before widening scope.

## Backend Conventions
- Keep service logic in the service layer, controller logic thin, and persistence in repository/entity classes under [backend/src/main/java/com/sena/backend](backend/src/main/java/com/sena/backend).
- Prefer constructor injection and transactional service methods when mutating data, matching patterns like [backend/src/main/java/com/sena/backend/service/impl/UserServiceImpl.java](backend/src/main/java/com/sena/backend/service/impl/UserServiceImpl.java).
- Preserve the existing error-response shape unless a consumer is updated at the same time; the global handler currently mixes DTO and map-based validation payloads in [backend/src/main/java/com/sena/backend/exception/GlobalExceptionHandler.java](backend/src/main/java/com/sena/backend/exception/GlobalExceptionHandler.java).
- Be careful with startup initialization and schema-dependent code because [backend/src/main/java/com/sena/backend/config/DataInitializer.java](backend/src/main/java/com/sena/backend/config/DataInitializer.java) seeds default roles at startup.

## Frontend Conventions
- The frontend is React plus Vite, with pages in [frontend/src/pages](frontend/src/pages) and shared state in [frontend/src/context](frontend/src/context).
- Use [frontend/src/main.jsx](frontend/src/main.jsx) as the active Vite entrypoint. [frontend/src/index.js](frontend/src/index.js) still exists, so check the build config before touching bootstrap code.
- Keep component changes consistent with the existing functional-component and hook-based style.

## Validation And Safety
- Before changing security, authentication, or authorization code, inspect the surrounding flow in [backend/src/main/java/com/sena/backend/security](backend/src/main/java/com/sena/backend/security) and related controllers/services.
- For frontend build issues, confirm the active entrypoint and Vite inputs before refactoring app bootstrap code.
- Avoid normalizing APIs or response formats as a side effect of unrelated work.

## When In Doubt
- Read the nearest controller, service, repository, or page that actually owns the behavior before editing.
- Prefer linking to documentation instead of duplicating project history or planning details here.
- If a change touches a pattern that repeats across the repo, consider whether a narrower file instruction or skill would be better than expanding this file.
