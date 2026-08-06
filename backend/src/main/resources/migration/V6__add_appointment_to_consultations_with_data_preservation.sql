-- V6__fix_concurrency_and_align_domain.sql

-- 1. Remove the flawed concurrency constraint from V4
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS uq_doctor_start_time;

-- 2. Add the column without constraints initially to allow existing rows to take NULL safely
ALTER TABLE consultations
    ADD COLUMN appointment_id BIGINT;

-- 3. DML Operation: Retrospectively generate missing appointments
DO $$
    DECLARE
        consultation_rec RECORD;
        new_appointment_id BIGINT;
        pat_id BIGINT;
    BEGIN
        -- Loop through all orphaned consultations
        FOR consultation_rec IN SELECT * FROM consultations WHERE appointment_id IS NULL LOOP

                -- Retrieve patient_id from the linked medical record
                SELECT patient_id INTO pat_id
                FROM medical_records
                WHERE id = consultation_rec.medical_record_id;

                -- Insert a synthetic appointment using the consultation's data
                INSERT INTO appointments (
                    patient_id, doctor_id, start_time, end_time, status, created_at, updated_at
                ) VALUES (
                             pat_id,
                             consultation_rec.doctor_id,
                             consultation_rec.consultation_date,
                             consultation_rec.consultation_date + INTERVAL '30 minutes',
                             'COMPLETED',
                             CURRENT_TIMESTAMP,
                             CURRENT_TIMESTAMP
                         ) RETURNING id INTO new_appointment_id;

                -- Link the newly created appointment back to the consultation
                UPDATE consultations
                SET appointment_id = new_appointment_id
                WHERE id = consultation_rec.id;

            END LOOP;
    END $$;

-- 4. DDL Operation: Apply strict constraints now that data integrity is guaranteed
-- Hard enforcement of the domain rule: A consultation must have an appointment
ALTER TABLE consultations
    ALTER COLUMN appointment_id SET NOT NULL;

ALTER TABLE consultations
    ADD CONSTRAINT uq_consultation_appointment UNIQUE (appointment_id);

ALTER TABLE consultations
    ADD CONSTRAINT fk_consultation_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id);