-- ISKOllect v2 - PostgreSQL local database schema
-- Create an empty database named iskollect_db, connect to it, then run this script.

BEGIN;

CREATE TABLE users (
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT users_username_not_blank CHECK (BTRIM(username) <> '')
);

CREATE TABLE students (
    student_id        SERIAL PRIMARY KEY,
    student_name      VARCHAR(100) NOT NULL,
    bottle_count      INTEGER NOT NULL DEFAULT 0,
    points_earned     NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT students_name_not_blank CHECK (BTRIM(student_name) <> ''),
    CONSTRAINT students_bottle_count_nonnegative CHECK (bottle_count >= 0),
    CONSTRAINT students_points_nonnegative CHECK (points_earned >= 0)
);

CREATE TABLE bottle_records (
    record_id         SERIAL PRIMARY KEY,
    student_id        INTEGER NOT NULL
        REFERENCES students(student_id) ON DELETE CASCADE,
    bottles_collected INTEGER NOT NULL,
    points_credited   NUMERIC(10,2) NOT NULL,
    submission_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    submission_time   TIME NOT NULL DEFAULT CURRENT_TIME,
    CONSTRAINT bottle_records_minimum CHECK (bottles_collected >= 5),
    CONSTRAINT bottle_records_points_positive CHECK (points_credited > 0)
);

CREATE TABLE points_ledger (
    ledger_id        SERIAL PRIMARY KEY,
    student_id       INTEGER NOT NULL
        REFERENCES students(student_id) ON DELETE CASCADE,
    points_change    NUMERIC(10,2) NOT NULL,
    source           VARCHAR(30) NOT NULL,
    ref_id           INTEGER NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT points_ledger_source_valid
        CHECK (source IN ('bottle_submission', 'redemption')),
    CONSTRAINT points_ledger_change_nonzero CHECK (points_change <> 0)
);

CREATE TABLE rewards_catalog (
    reward_id       SERIAL PRIMARY KEY,
    reward_name     VARCHAR(100) NOT NULL,
    description     VARCHAR(500) NOT NULL DEFAULT '',
    points_required NUMERIC(10,2) NOT NULL,
    available       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT rewards_catalog_name_not_blank CHECK (BTRIM(reward_name) <> ''),
    CONSTRAINT rewards_catalog_points_positive CHECK (points_required > 0)
);

CREATE TABLE redemptions (
    redemption_id   SERIAL PRIMARY KEY,
    student_id      INTEGER NOT NULL
        REFERENCES students(student_id) ON DELETE CASCADE,
    reward_id       INTEGER NOT NULL
        REFERENCES rewards_catalog(reward_id) ON DELETE RESTRICT,
    points_deducted NUMERIC(10,2) NOT NULL,
    redemption_date DATE NOT NULL DEFAULT CURRENT_DATE,
    redemption_time TIME NOT NULL DEFAULT CURRENT_TIME,
    CONSTRAINT redemptions_points_positive CHECK (points_deducted > 0)
);

CREATE UNIQUE INDEX users_username_key
    ON users (LOWER(username));
CREATE UNIQUE INDEX students_student_name_key
    ON students (LOWER(student_name));
CREATE UNIQUE INDEX rewards_catalog_reward_name_key
    ON rewards_catalog (LOWER(reward_name));
CREATE INDEX idx_bottle_records_student_date
    ON bottle_records (student_id, submission_date DESC, submission_time DESC);
CREATE INDEX idx_points_ledger_student_date
    ON points_ledger (student_id, transaction_date DESC);
CREATE INDEX idx_redemptions_student_date
    ON redemptions (student_id, redemption_date DESC, redemption_time DESC);

INSERT INTO rewards_catalog
    (reward_name, description, points_required, available)
VALUES
    ('Snack', 'Assorted school canteen snack', 25.00, TRUE),
    ('School supplies', 'Basic school supply bundle', 50.00, TRUE),
    ('Lunch', 'School canteen lunch meal', 75.00, TRUE);

COMMIT;

-- No administrator is seeded with a shared or plaintext password.
-- Start the application and select "Create the first administrator".