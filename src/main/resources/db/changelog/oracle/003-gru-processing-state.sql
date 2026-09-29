--liquibase formatted sql

--changeset illya:gru-005-status-timestamp
ALTER TABLE GRU.GRU_VISTA_TAB
    ADD FOC_STATUS_TS TIMESTAMP;

--rollback ALTER TABLE GRU.GRU_VISTA_TAB DROP COLUMN FOC_STATUS_TS;