-- user table
create table users (
    id bigserial primary key,
    username varchar(255) not null,
    email varchar(255) not null unique,
    hashed_password varchar(255) not null,
    birth_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
);

-- workout table
CREATE TABLE workouts (
    id BIGSERIAL PRIMARY KEY,
    type varchar(255),
    description varchar(500),
    user_id BIGINT NOT NULL,
    workout_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    duration_minutes int,
    CONSTRAINT fk_workout_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);