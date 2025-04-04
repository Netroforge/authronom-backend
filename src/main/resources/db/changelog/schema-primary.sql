--liquibase formatted sql
--changeset Yevhen Tienkaiev:20250326
--comment: Base

-- From https://github.com/kagkarlsson/db-scheduler/blob/master/db-scheduler/src/test/resources/postgresql_tables.sql
create table scheduled_tasks (
    task_name TEXT NOT NULL,
    task_instance TEXT NOT NULL,
    task_data bytea,
    execution_time TIMESTAMP WITH TIME ZONE NOT NULL,
    picked BOOLEAN NOT NULL,
    picked_by TEXT,
    last_success TIMESTAMP WITH TIME ZONE,
    last_failure TIMESTAMP WITH TIME ZONE,
    consecutive_failures INT,
    last_heartbeat TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL,
    priority SMALLINT,
    PRIMARY KEY (task_name, task_instance)
);

-- an optimization for users of priority might be to add priority to the execution_time_idx
-- this _might_ save reads as the priority-value is already in the index
CREATE INDEX execution_time_idx ON scheduled_tasks (execution_time ASC, priority DESC);
CREATE INDEX last_heartbeat_idx ON scheduled_tasks (last_heartbeat);
CREATE INDEX priority_execution_time_idx on scheduled_tasks (priority DESC, execution_time ASC);

-- From https://github.com/rocketbase-io/db-scheduler-log/blob/main/db-scheduler-log/src/test/resources/postgresql_tables.sql
create table scheduled_execution_logs (
    id BIGINT NOT NULL primary key,
    task_name TEXT NOT NULL,
    task_instance TEXT NOT NULL,
    task_data bytea,
    picked_by TEXT,
    time_started TIMESTAMP WITH TIME ZONE NOT NULL,
    time_finished TIMESTAMP WITH TIME ZONE NOT NULL,
    succeeded  BOOLEAN NOT NULL,
    duration_ms BIGINT NOT NULL,
    exception_class TEXT,
    exception_message TEXT,
    exception_stacktrace TEXT
);

CREATE INDEX stl_started_idx ON scheduled_execution_logs (time_started);
CREATE INDEX stl_task_name_idx ON scheduled_execution_logs (task_name);
CREATE INDEX stl_exception_class_idx ON scheduled_execution_logs (exception_class);

CREATE TABLE users_email_verifications (
    uid VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    confirmation_code VARCHAR(255) NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX users_email_verifications_email_confirmation_code_idx ON users_email_verifications USING btree (email, confirmation_code) WITH (FILLFACTOR = 30);

CREATE TABLE users (
    uid VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NULL,
    google_id VARCHAR(255) NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
