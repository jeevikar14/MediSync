CREATE SEQUENCE mrn_seq START 1001;

CREATE TABLE patients (
    id            BIGSERIAL PRIMARY KEY,
    mrn           VARCHAR(30)  NOT NULL UNIQUE,
    first_name    VARCHAR(80)  NOT NULL,
    last_name     VARCHAR(80)  NOT NULL,
    date_of_birth DATE         NOT NULL,
    gender        VARCHAR(20)  NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(120),
    address       VARCHAR(255),
    blood_group   VARCHAR(5),
    status        VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE app_users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);
