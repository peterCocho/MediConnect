-- V7__restore_medical_record_integrity.sql

-- 1. Transfer orphaned consultations to the primary medical record (oldest one) for each patient.
-- This guarantees we do not violate foreign keys when deleting duplicate records.
WITH RankedRecords AS (
    SELECT id, patient_id,
           ROW_NUMBER() OVER(PARTITION BY patient_id ORDER BY created_at ASC) as rn
    FROM medical_records
)
UPDATE consultations c
SET medical_record_id = rr.id
FROM medical_records mr
         JOIN RankedRecords rr ON mr.patient_id = rr.patient_id AND rr.rn = 1
WHERE c.medical_record_id = mr.id AND mr.id != rr.id;

-- 2. Delete duplicate medical records, keeping only the first one generated per patient.
WITH RankedRecords AS (
    SELECT id,
           ROW_NUMBER() OVER(PARTITION BY patient_id ORDER BY created_at ASC) as rn
    FROM medical_records
)
DELETE FROM medical_records
WHERE id IN (SELECT id FROM RankedRecords WHERE rn > 1);

-- 3. Drop the columns introduced in V2 that belong strictly to Consultations.
ALTER TABLE medical_records DROP CONSTRAINT IF EXISTS fk_mr_doctor;
ALTER TABLE medical_records DROP COLUMN IF EXISTS doctor_id;
ALTER TABLE medical_records DROP COLUMN IF EXISTS diagnosis;
ALTER TABLE medical_records DROP COLUMN IF EXISTS treatment;
ALTER TABLE medical_records DROP COLUMN IF EXISTS notes;

-- 4. Restore the UNIQUE constraint on patient_id to strictly enforce the 1:1 relationship at the DB level.
ALTER TABLE medical_records ADD CONSTRAINT uq_medical_records_patient_id UNIQUE (patient_id);