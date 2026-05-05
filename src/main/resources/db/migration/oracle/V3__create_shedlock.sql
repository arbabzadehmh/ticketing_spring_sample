CREATE TABLE shedlock (
                          name VARCHAR2(64) PRIMARY KEY,
                          lock_until TIMESTAMP NOT NULL,
                          locked_at TIMESTAMP NOT NULL,
                          locked_by VARCHAR2(255) NOT NULL
);
