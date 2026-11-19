--liquibase formatted sql

--changeset illya:gru-008-add-time-stamp-update failOnError:false
ALTER TABLE GRU.GRU_VISTA_TAB ADD (
    TIME_STAMP_UPDATE DATE
);

--rollback ALTER TABLE GRU.GRU_VISTA_TAB DROP COLUMN TIME_STAMP_UPDATE;
