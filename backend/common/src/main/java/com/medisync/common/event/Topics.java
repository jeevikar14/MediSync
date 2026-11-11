package com.medisync.common.event;

/** Kafka topic names - one topic per producing domain. */
public final class Topics {
    private Topics() {}
    public static final String PATIENT_EVENTS = "medisync.patient.events";
    public static final String CLINICAL_EVENTS = "medisync.clinical.events";
    public static final String APPOINTMENT_EVENTS = "medisync.appointment.events";
    public static final String CARE_EVENTS = "medisync.care.events";
    public static final String HANDOFF_EVENTS = "medisync.handoff.events";
}
