--liquibase formatted sql

--changeset illya:gru-006-processing-index
CREATE INDEX GRU.IDX_GRU_VISTA_PROCESSING
    ON GRU.GRU_VISTA_TAB
       (
           FOC_STATUS,
           FOC_TYPE,
           FOC_TS,
           ID
       );

--rollback DROP INDEX GRU.IDX_GRU_VISTA_PROCESSING;


--changeset illya:gru-007-status-time-index
CREATE INDEX GRU.IDX_GRU_VISTA_STATUS_TS
    ON GRU.GRU_VISTA_TAB
       (
           FOC_STATUS,
           FOC_STATUS_TS
       );

--rollback DROP INDEX GRU.IDX_GRU_VISTA_STATUS_TS;


--changeset illya:gru-008-file-index
CREATE INDEX GRU.IDX_GRU_VISTA_FILE_ID
    ON GRU.GRU_VISTA_TAB (FILE_ID);

--rollback DROP INDEX GRU.IDX_GRU_VISTA_FILE_ID;


--changeset illya:gru-009-reject-vista-index
CREATE INDEX GRU.IDX_GRU_REJECT_VISTA_ID
    ON GRU.GRU_REJECT_TAB (VISTA_TAB_ID);

--rollback DROP INDEX GRU.IDX_GRU_REJECT_VISTA_ID;


--changeset illya:gru-010-grants
GRANT SELECT, INSERT, UPDATE
    ON GRU.GRU_VISTA_TAB
    TO ACC_APP_ADAPTER;

GRANT SELECT
    ON GRU.GRU_VISTA_SEQ
    TO ACC_APP_ADAPTER;

GRANT SELECT, INSERT
    ON GRU.GRU_REJECT_TAB
    TO ACC_APP_ADAPTER;

GRANT SELECT
    ON GRU.GRU_REJECT_SEQ
    TO ACC_APP_ADAPTER;