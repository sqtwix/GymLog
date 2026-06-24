-- 1. user table
DO $$ BEGIN
    CREATE TYPE gender_enum AS ENUM ('F', 'M');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    gender gender_enum,
    hashed_password VARCHAR(255) NOT NULL,
    birth_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. type table (reference)
CREATE TABLE IF NOT EXISTS types (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL
);

-- 3. locations table (reference)
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    location VARCHAR(255) NOT NULL
);

-- 4. body_parts table (reference)
CREATE TABLE IF NOT EXISTS body_parts (
    id BIGSERIAL PRIMARY KEY,
    part_name VARCHAR(255) NOT NULL
);

-- 5. tips table (reference)
CREATE TABLE IF NOT EXISTS tips (
    id BIGSERIAL PRIMARY KEY,
    tip VARCHAR(255) NOT NULL
);

-- 6. challenges table (reference)
CREATE TABLE IF NOT EXISTS challenges (
    id BIGSERIAL PRIMARY KEY,
    challenge VARCHAR(255) NOT NULL
);

-- 7. workout table
CREATE TABLE IF NOT EXISTS workouts (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    description VARCHAR(500),
    user_id BIGINT NOT NULL,
    workout_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    duration_minutes INT,
    location_id BIGINT,
    CONSTRAINT fk_workout_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_type FOREIGN KEY (type_id) REFERENCES types (id) ON DELETE CASCADE,
    CONSTRAINT fk_locations FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL
);

-- 8. body_metrics table
CREATE TABLE IF NOT EXISTS body_metrics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    measured_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    weight_kg DECIMAL(5,2),
    height_cm INT,
    bmi DECIMAL(5,2),
    notes TEXT,
    CONSTRAINT fk_metrics_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 9. body_parts_workout table
CREATE TABLE IF NOT EXISTS body_parts_workout (
    id BIGSERIAL PRIMARY KEY,
    body_part_id BIGINT,
    workout_id BIGINT,
    CONSTRAINT fk_workout FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE,
    CONSTRAINT fk_body_part FOREIGN KEY (body_part_id) REFERENCES body_parts (id) ON DELETE CASCADE
);

-- 10. challenges_user
CREATE TABLE IF NOT EXISTS challenges_user (
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    CONSTRAINT fk_challenges_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_challanges FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE
);

-- 11. goals table
CREATE TABLE IF NOT EXISTS goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    start_date DATE NOT NULL,
    target_date DATE,
    achieved_at TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'active',
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. notification_settings
CREATE TABLE IF NOT EXISTS notification_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    reminder_hours INT DEFAULT 3,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 13. scheduled_workout_reminders table
CREATE TABLE IF NOT EXISTS scheduled_workout_reminders (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    scheduled_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_workout FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- indexes
CREATE INDEX IF NOT EXISTS idx_workouts_user_id ON workouts(user_id);
CREATE INDEX IF NOT EXISTS idx_workouts_type_id ON workouts(type_id);
CREATE INDEX IF NOT EXISTS idx_workouts_location_id ON workouts(location_id);
CREATE INDEX IF NOT EXISTS idx_body_metrics_user_id ON body_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_user_id ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_challenges_user_user_id ON challenges_user(user_id);
CREATE INDEX IF NOT EXISTS idx_challenges_user_challenge_id ON challenges_user(challenge_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_reminders_workout_id ON scheduled_workout_reminders(workout_id);

CREATE INDEX IF NOT EXISTS idx_workouts_user_date ON workouts(user_id, workout_date DESC);
CREATE INDEX IF NOT EXISTS idx_body_metrics_user_date ON body_metrics(user_id, measured_at DESC);

CREATE INDEX IF NOT EXISTS idx_reminders_pending ON scheduled_workout_reminders(scheduled_time, sent) WHERE sent = FALSE;
CREATE INDEX IF NOT EXISTS idx_goals_user_status ON goals(user_id, status);

-- 1. СОЗДАНИЕ ГРУППОВЫХ РОЛЕЙ (Без права логина, просто как шаблоны прав)
CREATE ROLE role_admin;
CREATE ROLE role_client;

-- 2. НАСТРОЙКА ПРАВ ДЛЯ АДМИНИСТРАТОРА
-- Даем доступ к схеме
GRANT USAGE, CREATE ON SCHEMA public TO role_admin;

-- Полный доступ ко всем существующим таблицам, последовательностям и процедурам
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO role_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO role_admin;
GRANT ALL PRIVILEGES ON ALL ROUTINES IN SCHEMA public TO role_admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO role_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO role_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON ROUTINES TO role_admin;


-- 3. НАСТРОЙКА ПРАВ ДЛЯ КЛИЕНТА
-- Даем доступ к схеме (только использование, без права создавать новые таблицы)
GRANT USAGE ON SCHEMA public TO role_client;

-- A. Только просмотр для справочных таблиц
GRANT SELECT ON
    types,
    locations,
    body_parts,
    tips,
    challenges
TO role_client;

-- B. Полный CRUD для пользовательских (транзакционных) данных
GRANT SELECT, INSERT, UPDATE, DELETE ON
    users,
    workouts,
    body_metrics,
    body_parts_workout,
    challenges_user,
    goals,
    notification_settings,
    scheduled_workout_reminders
TO role_client;

-- C. Доступ к генераторам ID (Очень важно! Без этого клиент не сможет делать INSERT)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO role_client;

-- D. Доступ к выполнению процедур, функций и триггеров
GRANT EXECUTE ON ALL ROUTINES IN SCHEMA public TO role_client;

-- 4. СОЗДАНИЕ РЕАЛЬНЫХ ПОЛЬЗОВАТЕЛЕЙ И ПРИСВОЕНИЕ РОЛЕЙ
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'role_admin') THEN
        CREATE ROLE role_admin;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'role_client') THEN
        CREATE ROLE role_client;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'gym_admin_user') THEN
        CREATE USER gym_admin_user WITH PASSWORD '${admin_password}';
        GRANT role_admin TO gym_admin_user;
        ALTER USER gym_admin_user CREATEROLE CREATEDB;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'gym_app_client') THEN
        CREATE USER gym_app_client WITH PASSWORD '${app_client_password}';
        GRANT role_client TO gym_app_client;
    END IF;
END
$$;

-- 1. Автоматический расчет ИМТ
-- Функция для триггера
CREATE OR REPLACE FUNCTION calculate_bmi()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.weight_kg IS NOT NULL AND NEW.height_cm IS NOT NULL AND NEW.height_cm > 0 THEN
        NEW.bmi := ROUND((NEW.weight_kg / ((NEW.height_cm::NUMERIC / 100) ^ 2))::NUMERIC, 2);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_bmi
BEFORE INSERT OR UPDATE ON body_metrics
FOR EACH ROW
EXECUTE FUNCTION calculate_bmi();

-- 2. Авто-обновление поля updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_notification_settings
BEFORE UPDATE ON notification_settings
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- 3. Авто-планирование напоминаний о тренировке
CREATE OR REPLACE FUNCTION auto_schedule_reminder()
RETURNS TRIGGER AS $$
DECLARE
    v_reminder_hours INT;
    v_enabled BOOLEAN;
BEGIN
    -- Достаем настройки пользователя
    SELECT enabled, reminder_hours INTO v_enabled, v_reminder_hours
    FROM notification_settings
    WHERE user_id = NEW.user_id;

    -- Если уведомления включены и тренировка в будущем
    IF v_enabled = TRUE AND NEW.workout_date > CURRENT_TIMESTAMP THEN
        INSERT INTO scheduled_workout_reminders (workout_id, user_id, scheduled_time)
        VALUES (
            NEW.id,
            NEW.user_id,
            NEW.workout_date - (v_reminder_hours || ' hours')::INTERVAL
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auto_schedule_reminder
AFTER INSERT ON workouts
FOR EACH ROW
EXECUTE FUNCTION auto_schedule_reminder();


-- 1. Получение отчёта о тренировках
CREATE OR REPLACE FUNCTION get_workout_report(
    p_user_id BIGINT,
    p_start_date TIMESTAMP,
    p_end_date TIMESTAMP
)
RETURNS TABLE (
    total_workouts BIGINT,
    total_hours NUMERIC,
    top_workout_types TEXT,
    top_muscle_groups TEXT
)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    -- 1. Считаем базовые метрики (кол-во тренировок и сумму минут)
    WITH stats AS (
        SELECT
            COUNT(w.id) AS t_workouts,
            COALESCE(SUM(w.duration_minutes), 0) AS t_minutes
        FROM workouts w
        WHERE w.user_id = p_user_id
          AND w.workout_date BETWEEN p_start_date AND p_end_date
    ),
    -- 2. Группируем виды тренировок (например: "Кардио (3), Силовая (2)")
    type_stats AS (
        SELECT t.type, COUNT(w.id) as cnt
        FROM workouts w
        JOIN types t ON w.type_id = t.id
        WHERE w.user_id = p_user_id
          AND w.workout_date BETWEEN p_start_date AND p_end_date
        GROUP BY t.type
        ORDER BY cnt DESC
    ),
    -- 3. Ищем самые популярные мышцы через связующую таблицу (топ-3)
    muscle_stats AS (
        SELECT bp.part_name, COUNT(bpw.workout_id) as cnt
        FROM workouts w
        JOIN body_parts_workout bpw ON w.id = bpw.workout_id
        JOIN body_parts bp ON bpw.body_part_id = bp.id
        WHERE w.user_id = p_user_id
          AND w.workout_date BETWEEN p_start_date AND p_end_date
        GROUP BY bp.part_name
        ORDER BY cnt DESC
        LIMIT 3
    )
    -- 4. Собираем всё в итоговую таблицу отчета
    SELECT
        s.t_workouts,
        ROUND(s.t_minutes / 60.0, 2) AS total_hours, -- Переводим минуты в часы
        (SELECT STRING_AGG(type || ' (' || cnt || ')', ', ') FROM type_stats),
        (SELECT STRING_AGG(part_name || ' (' || cnt || ')', ', ') FROM muscle_stats)
    FROM stats s;
END;
$$;

-- 2. Достижение цели
CREATE OR REPLACE PROCEDURE complete_goal(p_goal_id BIGINT)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE goals
    SET status = 'achieved',
        achieved_at = CURRENT_TIMESTAMP
    WHERE id = p_goal_id AND status != 'achieved';
END;
$$;

-- 3. Очистка пропущенных напоминаний
CREATE OR REPLACE PROCEDURE cleanup_missed_reminders()
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM scheduled_workout_reminders
    WHERE sent = FALSE AND scheduled_time < CURRENT_TIMESTAMP - INTERVAL '1 day';
END;
$$;