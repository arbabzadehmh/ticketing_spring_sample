CREATE TABLE application_log (
                                 id BIGINT PRIMARY KEY,
                                 log_level VARCHAR(10),
                                 logger VARCHAR(255),
                                 message TEXT,
                                 thread VARCHAR(100),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE application_log_seq
    START WITH 1
    INCREMENT BY 1;