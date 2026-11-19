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

    -- 4. Автоматическое расширение Redo Log журналов до 200 МБ для предотвращения Checkpoint not complete
    DECLARE
        v_large_redo_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_large_redo_count FROM v$log WHERE bytes >= 100*1024*1024;
        IF v_large_redo_count = 0 THEN
            BEGIN
                EXECUTE IMMEDIATE 'ALTER DATABASE ADD LOGFILE GROUP 3 SIZE 200M';
            EXCEPTION WHEN OTHERS THEN NULL;
            END;
            BEGIN
                EXECUTE IMMEDIATE 'ALTER DATABASE ADD LOGFILE GROUP 4 SIZE 200M';
            EXCEPTION WHEN OTHERS THEN NULL;
            END;
            BEGIN
                EXECUTE IMMEDIATE 'ALTER DATABASE ADD LOGFILE GROUP 5 SIZE 200M';
            EXCEPTION WHEN OTHERS THEN NULL;
            END;
        END IF;
    END;
END;
/
