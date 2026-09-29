--liquibase formatted sql

--changeset illya:pom-005-unique-filename
ALTER TABLE pom.file
    ADD CONSTRAINT uk_pom_file_filename
        UNIQUE (filename);

--rollback ALTER TABLE pom.file DROP CONSTRAINT IF EXISTS uk_pom_file_filename;


--changeset illya:pom-006-index-file-status
CREATE INDEX idx_pom_file_status
    ON pom.file (file_status);

--rollback DROP INDEX IF EXISTS pom.idx_pom_file_status;


--changeset illya:pom-007-index-unit-file
CREATE INDEX idx_pom_unit_file_id
    ON pom.unit (file_id);

--rollback DROP INDEX IF EXISTS pom.idx_pom_unit_file_id;


--changeset illya:pom-008-index-unit-error-file
CREATE INDEX idx_pom_unit_error_file_id
    ON pom.unit_error (file_id);

--rollback DROP INDEX IF EXISTS pom.idx_pom_unit_error_file_id;


--changeset illya:pom-009-index-unit-error-unit
CREATE INDEX idx_pom_unit_error_unit_id
    ON pom.unit_error (unit_id);

--rollback DROP INDEX IF EXISTS pom.idx_pom_unit_error_unit_id;