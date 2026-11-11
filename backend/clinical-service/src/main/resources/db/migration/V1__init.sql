CREATE TABLE lab_orders (
    id         BIGSERIAL PRIMARY KEY,
    patient_id BIGINT       NOT NULL,
    test_name  VARCHAR(120) NOT NULL,
    priority   VARCHAR(15)  NOT NULL DEFAULT 'ROUTINE',
    ordered_by VARCHAR(50)  NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ORDERED',
    notes      VARCHAR(500),
    ordered_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_lab_orders_patient ON lab_orders(patient_id);

CREATE TABLE lab_reports (
    id             BIGSERIAL PRIMARY KEY,
    lab_order_id   BIGINT       NOT NULL REFERENCES lab_orders(id),
    patient_id     BIGINT       NOT NULL,
    test_name      VARCHAR(120) NOT NULL,
    result_summary VARCHAR(1000) NOT NULL,
    abnormal       BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_by    VARCHAR(50)  NOT NULL,
    uploaded_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    reviewed       BOOLEAN      NOT NULL DEFAULT FALSE,
    reviewed_by    VARCHAR(50),
    reviewed_at    TIMESTAMPTZ,
    review_comment VARCHAR(500)
);
CREATE INDEX idx_lab_reports_patient ON lab_reports(patient_id);

CREATE TABLE clinical_notes (
    id         BIGSERIAL PRIMARY KEY,
    patient_id BIGINT      NOT NULL,
    note_type  VARCHAR(30) NOT NULL,
    content    TEXT        NOT NULL,
    author     VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notes_patient ON clinical_notes(patient_id);
