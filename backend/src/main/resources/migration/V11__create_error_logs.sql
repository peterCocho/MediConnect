-- Creates the table to persist system exceptions and errors
CREATE TABLE error_logs (
                            id BIGSERIAL PRIMARY KEY,
                            timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
                            status_code INTEGER NOT NULL,
                            exception_type VARCHAR(255) NOT NULL,
                            message TEXT,
                            stack_trace TEXT,
                            path VARCHAR(255)
);