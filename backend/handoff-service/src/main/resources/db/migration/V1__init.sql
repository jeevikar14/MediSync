CREATE TABLE handoffs (
    id               BIGSERIAL PRIMARY KEY,
    patient_id       BIGINT       NOT NULL,
    from_doctor      VARCHAR(50)  NOT NULL,
    to_doctor        VARCHAR(50)  NOT NULL,
    from_department  VARCHAR(80),
    to_department    VARCHAR(80),
    reason           VARCHAR(255),
    clinical_summary TEXT         NOT NULL,
    status           VARCHAR(15)  NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    acknowledged_at  TIMESTAMPTZ,
    acknowledged_by  VARCHAR(50),
    ack_comment      VARCHAR(500)
);
CREATE INDEX idx_handoffs_patient ON handoffs(patient_id);
CREATE INDEX idx_handoffs_to ON handoffs(to_doctor, status);

CREATE TABLE handoff_items (
    id           BIGSERIAL PRIMARY KEY,
    handoff_id   BIGINT       NOT NULL REFERENCES handoffs(id),
    item_type    VARCHAR(30)  NOT NULL,
    description  VARCHAR(300) NOT NULL,
    reference_id VARCHAR(64)
);

CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    recipient  VARCHAR(60)  NOT NULL,
    type       VARCHAR(40)  NOT NULL,
    message    VARCHAR(500) NOT NULL,
    patient_id BIGINT,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_recipient ON notifications(recipient, is_read);

-- local projection of care gaps (built from CARE_GAP_* events) used to suggest handoff items
CREATE TABLE known_gaps (
    gap_id      BIGINT PRIMARY KEY,
    patient_id  BIGINT       NOT NULL,
    gap_type    VARCHAR(30)  NOT NULL,
    description VARCHAR(500) NOT NULL,
    severity    VARCHAR(10)  NOT NULL,
    status      VARCHAR(10)  NOT NULL
);
CREATE INDEX idx_known_gaps_patient ON known_gaps(patient_id, status);
