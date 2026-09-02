CREATE SCHEMA IF NOT EXISTS pom;

CREATE TABLE IF NOT EXISTS pom.file (
    id BIGSERIAL PRIMARY KEY,
    ins_time TIMESTAMP WITH TIME ZONE NOT NULL,
    filename VARCHAR(40),
    fullpath VARCHAR(120),
    sender VARCHAR(20),
    file_comment VARCHAR(100),
    upd_time TIMESTAMP WITH TIME ZONE,
    file_status VARCHAR(10),
    uli_date VARCHAR(3)
);

CREATE TABLE IF NOT EXISTS pom.unit (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT REFERENCES pom.file(id),
    ins_time TIMESTAMP NOT NULL,
    pom_type VARCHAR(3),
    status VARCHAR(10),
    unit_value VARCHAR(2000),
    upd_time TIMESTAMP,
    add_value VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS pom.unit_error (
    id BIGSERIAL PRIMARY KEY,
    unit_id BIGINT REFERENCES pom.unit(id),
    error_seq INTEGER,
    error_code VARCHAR(3),
    error_field VARCHAR(2000),
    error_msg VARCHAR(1000),
    file_id BIGINT REFERENCES pom.file(id)
);