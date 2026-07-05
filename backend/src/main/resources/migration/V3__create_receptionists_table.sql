-- 1. MIGRACIÓN SQL: V3__create_receptionists_table.sql
CREATE TABLE receptionists (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL UNIQUE,
                               identity_document VARCHAR(20) NOT NULL UNIQUE,
                               full_name VARCHAR(150) NOT NULL,
                               phone VARCHAR(20) NOT NULL,
                               CONSTRAINT fk_receptionist_user FOREIGN KEY (user_id) REFERENCES users(id)
);