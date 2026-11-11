CREATE TABLE appointments (
    id             BIGSERIAL PRIMARY KEY,
    patient_id     BIGINT       NOT NULL,
    doctor_name    VARCHAR(100) NOT NULL,
    department     VARCHAR(80)  NOT NULL,
    scheduled_at   TIMESTAMPTZ  NOT NULL,
    reason         VARCHAR(255),
    follow_up      BOOLEAN      NOT NULL DEFAULT FALSE,
    status         VARCHAR(15)  NOT NULL DEFAULT 'BOOKED',
    notes          VARCHAR(500),
    created_by     VARCHAR(50)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_appt_patient ON appointments(patient_id);
CREATE INDEX idx_appt_status ON appointments(status);
