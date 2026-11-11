CREATE TABLE timeline_events (
    id           BIGSERIAL PRIMARY KEY,
    event_id     VARCHAR(64)  NOT NULL UNIQUE,
    patient_id   BIGINT       NOT NULL,
    event_type   VARCHAR(40)  NOT NULL,
    reference_id VARCHAR(64),
    summary      VARCHAR(500) NOT NULL,
    actor        VARCHAR(50),
    occurred_at  TIMESTAMPTZ  NOT NULL,
    details      TEXT
);
CREATE INDEX idx_timeline_patient ON timeline_events(patient_id, occurred_at);

CREATE TABLE care_gaps (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT       NOT NULL,
    gap_type        VARCHAR(30)  NOT NULL,
    reference_id    VARCHAR(64),
    description     VARCHAR(500) NOT NULL,
    severity        VARCHAR(10)  NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'OPEN',
    escalated       BOOLEAN      NOT NULL DEFAULT FALSE,
    detected_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    resolved_at     TIMESTAMPTZ,
    resolution_note VARCHAR(255)
);
CREATE INDEX idx_gaps_patient ON care_gaps(patient_id, status);

CREATE TABLE pathways (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(40)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(300)
);

CREATE TABLE pathway_steps (
    id            BIGSERIAL PRIMARY KEY,
    pathway_id    BIGINT       NOT NULL REFERENCES pathways(id),
    step_order    INT          NOT NULL,
    activity_type VARCHAR(40)  NOT NULL,
    description   VARCHAR(200) NOT NULL,
    mandatory     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE care_episodes (
    id            BIGSERIAL PRIMARY KEY,
    patient_id    BIGINT       NOT NULL,
    pathway_id    BIGINT       NOT NULL REFERENCES pathways(id),
    pathway_name  VARCHAR(100) NOT NULL,
    status        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    opened_by     VARCHAR(50),
    opened_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    closed_at     TIMESTAMPTZ,
    closing_notes VARCHAR(500)
);
CREATE INDEX idx_episodes_patient ON care_episodes(patient_id, status);

CREATE TABLE episode_steps (
    id            BIGSERIAL PRIMARY KEY,
    episode_id    BIGINT       NOT NULL REFERENCES care_episodes(id),
    step_order    INT          NOT NULL,
    activity_type VARCHAR(40)  NOT NULL,
    description   VARCHAR(200) NOT NULL,
    mandatory     BOOLEAN      NOT NULL,
    status        VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    completed_at  TIMESTAMPTZ,
    completed_by  VARCHAR(50),
    evidence_ref  VARCHAR(100)
);
CREATE INDEX idx_episode_steps_episode ON episode_steps(episode_id);

-- ---- seed clinical pathways ----
INSERT INTO pathways(code, name, description) VALUES
 ('DIABETES_REVIEW', 'Diabetes Review Pathway', 'Lab work-up, doctor review and follow-up consultation'),
 ('POST_SURGERY_FOLLOWUP', 'Post-Surgery Follow-up Pathway', 'Discharge note, booked and completed follow-up visit');

INSERT INTO pathway_steps(pathway_id, step_order, activity_type, description, mandatory)
SELECT id, 1, 'LAB_ORDERED',           'Lab panel ordered (e.g. HbA1c)',        TRUE  FROM pathways WHERE code='DIABETES_REVIEW' UNION ALL
SELECT id, 2, 'LAB_REPORT_UPLOADED',   'Lab report uploaded by laboratory',     TRUE  FROM pathways WHERE code='DIABETES_REVIEW' UNION ALL
SELECT id, 3, 'LAB_REPORT_REVIEWED',   'Lab report reviewed by a doctor',       TRUE  FROM pathways WHERE code='DIABETES_REVIEW' UNION ALL
SELECT id, 4, 'APPOINTMENT_COMPLETED', 'Follow-up consultation completed',      TRUE  FROM pathways WHERE code='DIABETES_REVIEW' UNION ALL
SELECT id, 5, 'CLINICAL_NOTE_ADDED',   'Clinical note recorded (recommended)',  FALSE FROM pathways WHERE code='DIABETES_REVIEW';

INSERT INTO pathway_steps(pathway_id, step_order, activity_type, description, mandatory)
SELECT id, 1, 'CLINICAL_NOTE_ADDED',   'Discharge / progress note recorded',    TRUE  FROM pathways WHERE code='POST_SURGERY_FOLLOWUP' UNION ALL
SELECT id, 2, 'APPOINTMENT_BOOKED',    'Follow-up appointment booked',          TRUE  FROM pathways WHERE code='POST_SURGERY_FOLLOWUP' UNION ALL
SELECT id, 3, 'APPOINTMENT_COMPLETED', 'Follow-up visit completed',             TRUE  FROM pathways WHERE code='POST_SURGERY_FOLLOWUP' UNION ALL
SELECT id, 4, 'LAB_REPORT_REVIEWED',   'Post-op lab report reviewed (optional)', FALSE FROM pathways WHERE code='POST_SURGERY_FOLLOWUP';
