-- 1. Expand column lengths to prevent truncation errors for current and future statuses
ALTER TABLE appointments ALTER COLUMN status TYPE VARCHAR(30);
ALTER TABLE consultations ALTER COLUMN status TYPE VARCHAR(30);

-- 2. Normalize appointment statuses before mapping them to the Java enum
UPDATE consultations c
SET status = 'PENDING_CONFIRMATION'
FROM appointments a
WHERE c.appointment_id = a.id
  AND a.status = 'BOOKED';

UPDATE appointments
SET status = 'PENDING_CONFIRMATION'
WHERE status = 'BOOKED';