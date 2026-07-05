-- V2__add_clinical_details_to_medical_records.sql

-- 1. Añadimos el vínculo al médico que atiende (fk_mr_doctor)
ALTER TABLE medical_records
    ADD COLUMN doctor_id BIGINT NOT NULL;

-- 2. Añadimos los campos de contenido médico
ALTER TABLE medical_records
    ADD COLUMN diagnosis TEXT NOT NULL,
    ADD COLUMN treatment TEXT,
    ADD COLUMN notes TEXT;

-- 3. Establecemos la restricción de llave foránea al doctor
ALTER TABLE medical_records
    ADD CONSTRAINT fk_mr_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id);

-- 4. Ajuste crítico: Quitamos UNIQUE de patient_id
-- Un paciente tendrá múltiples visitas/registros, por lo tanto no puede ser UNIQUE
ALTER TABLE medical_records DROP CONSTRAINT medical_records_patient_id_key;