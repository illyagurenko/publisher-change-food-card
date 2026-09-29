--liquibase formatted sql

--changeset illya:pom-001
CREATE SCHEMA IF NOT EXISTS pom;

--rollback DROP SCHEMA IF EXISTS pom CASCADE;