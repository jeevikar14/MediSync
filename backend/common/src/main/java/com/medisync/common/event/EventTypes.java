package com.medisync.common.event;

/** Event type names carried in {@link DomainEvent#eventType()}. */
public final class EventTypes {
    private EventTypes() {}
    // patient-service
    public static final String PATIENT_REGISTERED = "PATIENT_REGISTERED";
    // clinical-service
    public static final String LAB_ORDERED = "LAB_ORDERED";
    public static final String LAB_REPORT_UPLOADED = "LAB_REPORT_UPLOADED";
    public static final String LAB_REPORT_REVIEWED = "LAB_REPORT_REVIEWED";
    public static final String CLINICAL_NOTE_ADDED = "CLINICAL_NOTE_ADDED";
    // appointment-service
    public static final String APPOINTMENT_BOOKED = "APPOINTMENT_BOOKED";
    public static final String APPOINTMENT_COMPLETED = "APPOINTMENT_COMPLETED";
    public static final String APPOINTMENT_MISSED = "APPOINTMENT_MISSED";
    // care-orchestrator-service
    public static final String CARE_GAP_DETECTED = "CARE_GAP_DETECTED";
    public static final String CARE_GAP_RESOLVED = "CARE_GAP_RESOLVED";
    public static final String CARE_GAP_ESCALATED = "CARE_GAP_ESCALATED";
    public static final String EPISODE_STARTED = "EPISODE_STARTED";
    public static final String EPISODE_CLOSED = "EPISODE_CLOSED";
    // handoff-service
    public static final String HANDOFF_CREATED = "HANDOFF_CREATED";
    public static final String HANDOFF_ACKNOWLEDGED = "HANDOFF_ACKNOWLEDGED";
}
