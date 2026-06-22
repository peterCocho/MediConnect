-- V1__init_schema_mediconnect.sql

-- ==========================================
-- 1. SECURITY MODULE (RBAC) - CORREGIDO
-- ==========================================
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255)
);

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role_id INT NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ==========================================
-- 2. RESOURCES MODULE (DOCTORS)
-- ==========================================
CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    specialty VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- El vínculo crucial con el sistema de acceso
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- 3. PATIENTS MODULE
-- ==========================================
CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    identity_document VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    birth_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ==========================================
-- 4. CLINICAL RECORDS MODULE (Logical Container)
-- ==========================================
CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    record_number VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    patient_id BIGINT NOT NULL UNIQUE, 
    CONSTRAINT fk_mr_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- Entidad Maestra para el módulo de notas clínicas flexibles
CREATE TABLE clinical_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    template_content TEXT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. SCHEDULING MODULE (Appointments)
-- ==========================================
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    appointment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL, -- e.g., SCHEDULED, COMPLETED, CANCELED
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    cancellation_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- ==========================================
-- 6. CLINICAL MODULE (Hybrid Model)
-- ==========================================
CREATE TABLE consultations (
    id BIGSERIAL PRIMARY KEY,
    consultation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL, 
    
    -- Structured Core
    systolic_pressure INTEGER CHECK (systolic_pressure > 0 AND systolic_pressure < 300),
    diastolic_pressure INTEGER CHECK (diastolic_pressure > 0 AND diastolic_pressure < 200),
    heart_rate INTEGER CHECK (heart_rate > 0 AND heart_rate < 300),
    weight NUMERIC(5,2) CHECK (weight > 0),
    icd10_code VARCHAR(10),
    
    -- Flexible Core (Text areas)
    reason_for_visit TEXT,
    clinical_notes TEXT,
    management_plan TEXT,
    cancellation_reason VARCHAR(255),
    
    -- Relationships
    doctor_id BIGINT NOT NULL,
    medical_record_id BIGINT NOT NULL,
    CONSTRAINT fk_consultation_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_consultation_mr FOREIGN KEY (medical_record_id) REFERENCES medical_records(id)
);

-- ==========================================
-- 7. AUTOMATION MODULE (n8n Callback)
-- ==========================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    destination_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, SENT, FAILED
    provider_id VARCHAR(100), 
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consultation_id BIGINT NOT NULL,
    CONSTRAINT fk_notification_consultation FOREIGN KEY (consultation_id) REFERENCES consultations(id)
);

-- ==========================================
-- 8. SYSTEM CONFIGURATION MODULE
-- ==========================================
CREATE TABLE system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- INDEXES & CONSTRAINTS (Performance)
-- ==========================================
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_consultations_date ON consultations(consultation_date);
CREATE INDEX idx_consultations_status ON consultations(status);
CREATE INDEX idx_notifications_status ON notifications(status);

-- ==========================================
-- DATA SEEDING (Valores de rescate base)
-- ==========================================
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrador del sistema'),
('ROLE_DOCTOR', 'Médico especialista'),
('ROLE_RECEPTION', 'Recepcionista y admisiones');

INSERT INTO clinical_templates (name, description, template_content) VALUES 
('Formato SOAP', 'Plantilla general subjetivo/objetivo/análisis/plan', 'S: \nO: \nA: \nP: '),
('Control Especialidad', 'Plantilla rápida para controles de rutina', 'Motivo de consulta: \nEvolución: \nPlan a seguir: ');

INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('N8N_WEBHOOK_URL', 'http://localhost:5678/webhook/test', 'URL del webhook receptor en n8n'),
('N8N_API_KEY', 'clave_desarrollo_123', 'Token de seguridad para la conexión con n8n');