--liquibase formatted sql

--changeset illya:gru-001-sequence
CREATE SEQUENCE GRU.GRU_VISTA_SEQ
    START WITH 1
    INCREMENT BY 1
    CACHE 5000;

--rollback DROP SEQUENCE GRU.GRU_VISTA_SEQ;


--changeset illya:gru-002-vista-table
CREATE TABLE GRU.GRU_VISTA_TAB
(
    ID                 NUMBER(12),
    SYSTEMACCOUNT      VARCHAR2(32 BYTE),
    CURRENCY           VARCHAR2(3 BYTE),
    XALFA              NUMBER(23,3),
    OPERATION          VARCHAR2(3 BYTE),
    TIME_STAMP         DATE                       DEFAULT sysdate,
    POM_ID             NUMBER(12),
    UTERRARIO          NUMBER(18),
    OLDTBAL            NUMBER(23,3),
    NEWTBAL            NUMBER(23,3),
    ADD_INFO           VARCHAR2(100 BYTE),
    FILE_ID            NUMBER(12),
    FOC_STATUS         VARCHAR2(64 BYTE)          DEFAULT 'WAIT',
    FOC_TS             DATE,
    FOC_TYPE           VARCHAR2(10 BYTE)          DEFAULT 'IMMEDIATE',
    SVFE_LOAD_ID       VARCHAR2(36 BYTE)          DEFAULT 'None',

    CONSTRAINT pk_gru_vista_tab
        PRIMARY KEY (ID)
);

--rollback DROP TABLE GRU.GRU_VISTA_TAB;