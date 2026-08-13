CREATE TABLE whatsapp_message_logs (
                                       id BIGSERIAL PRIMARY KEY,
                                       phone_number VARCHAR(20) NOT NULL,
                                       message_body TEXT NOT NULL,
                                       received_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       is_read BOOLEAN NOT NULL DEFAULT FALSE
);