-- 1. user table
CREATE TYPE gender_enum AS ENUM ('F', 'M');

create table users (
    id bigserial primary key,
    username varchar(255) not null,
    email varchar(255) not null unique,
    gender gender_enum,
    hashed_password varchar(255) not null,
    birth_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
);

-- 2. workout table
CREATE TABLE workouts (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT not null,
    description varchar(500),
    user_id BIGINT NOT NULL,
    workout_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    duration_minutes int,
    location_id BIGINT,
    CONSTRAINT fk_workout_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_type FOREIGN KEY (type_id) REFERENCES types (id) ON DELETE CASCADE,
    CONSTRAINT fk_locations FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL
);

-- 3. type table (reference)
CREATE TABLE types (
    id BIGSERIAL primary key,
    type varchar(255) not null
);

-- 4. body_metrics table
CREATE TABLE body_metrics (
    id BIGSERIAL primary key,
    user_id bigint not null,
    measured_at	TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    weight_kg DECIMAL(5,2),
    height_cm int,
    bmi decimal(5,2),
    notes TEXT,
    CONSTRAINT fk_metrics_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 5. body_parts table (reference)
CREATE table body_parts (
    id BIGSERIAL primary key,
    part_name varchar(255) not null,
);

-- 6. body_parts_workout table
CREATE table body_parts_workout (
    id BIGSERIAL primary key,
    body_part_id BIGINT,
    workout_id BIGINT,
    CONSTRAINT fk_workout FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE,
    CONSTRAINT fk_body_part FOREIGN KEY (body_part_id) REFERENCES body_parts (id) ON DELETE CASCADE
);

-- 7. locations table (reference)
create table locations (
    id BIGSERIAL primary key,
    location varchar(255) not null
);

-- 8. tips table (reference)
create table tips (
    id BIGSERIAL primary key,
    tip varchar(255) not null
);

-- 9. challenges table (reference)
create table challenges (
    id BIGSERIAL primary key,
    challenge varchar(255) not null
);

-- 10. challenges_user
create table challenges_user(
    user_id BIGINT NOT NULL,
    challenge_id BIGINT not null,
    CONSTRAINT fk_challenges_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_challanges FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE
);

-- 11. goals table
CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_type VARCHAR(50) NOT NULL, -- 'weight', 'workout_frequency', 'exercise_pr'
    target_value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20), -- 'kg', 'times_per_week'
    start_date DATE NOT NULL,
    target_date DATE,
    achieved_at TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(20) DEFAULT 'active', -- 'active', 'achieved', 'abandoned'
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. water_intake table
CREATE TABLE water_intake (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount_ml INT NOT NULL,
    logged_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_water_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 13. sleep_logs
CREATE TABLE sleep_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sleep_date DATE NOT NULL,
    hours DECIMAL(3,1) NOT NULL,
    quality_score INT CHECK (quality_score BETWEEN 1 AND 5),
    notes TEXT,
    CONSTRAINT fk_sleep_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, sleep_date)
);

-- 14. notification_settings
CREATE TABLE notification_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    reminder_hours INT DEFAULT 3,      -- за сколько часов до тренировки напоминать
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 15. scheduled_workout_reminders table
CREATE TABLE scheduled_workout_reminders (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    scheduled_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,  -- время, когда нужно отправить уведомление
    sent BOOLEAN DEFAULT FALSE,                           -- отправлено ли уже
    sent_at TIMESTAMP WITHOUT TIME ZONE,                  -- фактическое время отправки
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_workout FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- indexes
CREATE INDEX idx_workouts_user_id ON workouts(user_id);
CREATE INDEX idx_workouts_type_id ON workouts(type_id);
CREATE INDEX idx_workouts_location_id ON workouts(location_id);
CREATE INDEX idx_body_metrics_user_id ON body_metrics(user_id);
CREATE INDEX idx_water_intake_user_id ON water_intake(user_id);
CREATE INDEX idx_sleep_logs_user_id ON sleep_logs(user_id);
CREATE INDEX idx_goals_user_id ON goals(user_id);
CREATE INDEX idx_challenges_user_user_id ON challenges_user(user_id);
CREATE INDEX idx_challenges_user_challenge_id ON challenges_user(challenge_id);
CREATE INDEX idx_scheduled_reminders_workout_id ON scheduled_workout_reminders(workout_id);

CREATE INDEX idx_workouts_user_date ON workouts(user_id, workout_date DESC);
CREATE INDEX idx_body_metrics_user_date ON body_metrics(user_id, measured_at DESC);
CREATE INDEX idx_water_intake_user_date ON water_intake(user_id, logged_at DESC);
CREATE INDEX idx_sleep_logs_user_date ON sleep_logs(user_id, sleep_date DESC);

CREATE INDEX idx_reminders_pending ON scheduled_workout_reminders(scheduled_time, sent) WHERE sent = FALSE;
CREATE INDEX idx_goals_user_status ON goals(user_id, status);