--liquibase formatted sql
--changeset Yevhen Tienkaiev:20250326
--comment: Base

CREATE TABLE users (
    uid VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    first_name VARCHAR(255) NULL,
    last_name VARCHAR(255) NULL,
    phone_number VARCHAR(20) NULL,
    roles VARCHAR(20)[] NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
