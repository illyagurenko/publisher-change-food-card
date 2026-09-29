--liquibase formatted sql

--changeset illya:pom-002-file
CREATE TABLE pom.file
(
    id           BIGSERIAL PRIMARY KEY,
    ins_time     TIMESTAMP WITH TIME ZONE NOT NULL,
    filename     VARCHAR(40),
    fullpath     VARCHAR(120),
    sender       VARCHAR(20),
    file_comment VARCHAR(100),
    upd_time     TIMESTAMP WITH TIME ZONE,
    file_status  VARCHAR(10),
    uli_date     VARCHAR(3)
);

--rollback DROP TABLE IF EXISTS pom.file CASCADE;


--changeset illya:pom-003-unit
CREATE TABLE pom.unit
(
    id         BIGSERIAL PRIMARY KEY,
    file_id    BIGINT,
    ins_time   TIMESTAMP NOT NULL,
    pom_type   VARCHAR(3),
    status     VARCHAR(10),
    unit_value VARCHAR(2000),
    upd_time   TIMESTAMP,
    add_value  VARCHAR(100),

    CONSTRAINT fk_pom_unit_file
        FOREIGN KEY (file_id)
        REFERENCES pom.file(id)
        ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS pom.unit CASCADE;


--changeset illya:pom-004-unit-error
CREATE TABLE pom.unit_error
(
    id          BIGSERIAL PRIMARY KEY,
    file_id     BIGINT,
    unit_id     BIGINT,
    error_seq   INTEGER,
    error_code  VARCHAR(3),
    error_field VARCHAR(2000),
    error_msg   VARCHAR(1000),

    CONSTRAINT fk_pom_unit_error_file
        FOREIGN KEY (file_id)
        REFERENCES pom.file(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pom_unit_error_unit
        FOREIGN KEY (unit_id)
        REFERENCES pom.unit(id)
        ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS pom.unit_error CASCADE;