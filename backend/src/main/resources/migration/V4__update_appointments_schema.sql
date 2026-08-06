ALTER TABLE appointments RENAME COLUMN appointment_date TO start_time;

ALTER TABLE appointments ADD COLUMN end_time TIMESTAMP WITH TIME ZONE NOT NULL;
ALTER TABLE appointments ADD COLUMN notes VARCHAR(255);
ALTER TABLE appointments ADD COLUMN version INT DEFAULT 0;
ALTER TABLE appointments ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE appointments ADD CONSTRAINT uq_doctor_start_time UNIQUE (doctor_id, start_time);