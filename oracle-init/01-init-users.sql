-- oracle-init/01-init-users.sql
-- Автоматическая инициализация пользователей при старте контейнера Oracle XE
ALTER SESSION SET CURRENT_SCHEMA = SYSTEM;

DECLARE
    v_user_count NUMBER;
BEGIN
    -- 1. Создаем пользователя ACC_APP_ADAPTER, если его еще нет
    SELECT COUNT(*) INTO v_user_count FROM all_users WHERE username = 'ACC_APP_ADAPTER';
    IF v_user_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE USER ACC_APP_ADAPTER IDENTIFIED BY root DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS';
    END IF;

    -- 2. Выдаем полные системные права для адаптера (сессии, таблицы, сиквенсы)
    EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CONNECT, RESOURCE TO ACC_APP_ADAPTER';
    EXECUTE IMMEDIATE 'GRANT UNLIMITED TABLESPACE TO ACC_APP_ADAPTER';

    -- 3. Пользователь GRU создается контейнером автоматически через переменные APP_USER/APP_USER_PASSWORD,
    -- но явно гарантируем ему необходимые квоты и права
    EXECUTE IMMEDIATE 'GRANT UNLIMITED TABLESPACE TO GRU';
    EXECUTE IMMEDIATE 'GRANT CREATE USER TO GRU';
END;
/
