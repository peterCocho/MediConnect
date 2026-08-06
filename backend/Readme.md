# MediConnect - Backend API

## Prerequisites
- Java 17
- PostgreSQL 17 (Configured to run on port 8081)
- Maven 3.8+

## Database Setup
Create a new PostgreSQL database named `mediconnect`. Flyway will automatically handle schema migrations (V1 to V3) upon application startup.

## Environment Variables
Before running the application, the following environment variables must be configured in your system or IDE to ensure proper database connection and security initialization:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:8081/mediconnect`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=your_db_password`
- `JWT_SECRET=your_jwt_secret_key_minimum_256_bits`
- `ADMIN_SETUP_PASSWORD=your_admin_bootstrap_password`

## Installation & Execution
1. Clean and build the project:
   ```bash
   mvn clean install -DskipTests

2. Run the application:

   ```bash
   mvn spring-boot:run
   ```

## API Testing
An Insomnia collection file (`MediConnect-Solicitudes-HTTP.yaml`) is included in the root of this repository. Import this file directly into Insomnia to test all available endpoints, including error handling and role-based access control.

