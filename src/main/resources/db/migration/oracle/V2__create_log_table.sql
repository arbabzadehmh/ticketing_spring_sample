-- 1. Create the table without the identity column
CREATE TABLE application_log (
    id NUMBER PRIMARY KEY,
    log_level VARCHAR2(10),
    logger VARCHAR2(255),
    message CLOB,
    thread VARCHAR2(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create the sequence for the ID
CREATE SEQUENCE application_log_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
