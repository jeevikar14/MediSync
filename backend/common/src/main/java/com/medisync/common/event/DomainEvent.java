package com.medisync.common.event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The single event contract shared by all services (serialised as JSON on Kafka).
 *
 * @param eventId     unique id, used by consumers for idempotency
 * @param eventType   one of {@link EventTypes}
 * @param patientId   the patient this event belongs to (also the Kafka message key)
 * @param referenceId id of the business object (lab report id, appointment id, handoff id ...)
 * @param actor       username that caused the event
 * @param summary     human readable one-liner (used in the patient journey timeline)
 * @param occurredAt  event time
 * @param details     type specific string attributes (e.g. testName, doctor, followUp)
 */
public record DomainEvent(String eventId, String eventType, Long patientId, String referenceId,
                          String actor, String summary, Instant occurredAt, Map<String, String> details) {

    public static DomainEvent of(String type, Long patientId, Object referenceId, String actor,
                                 String summary, Map<String, String> details) {
        Map<String, String> safe = new HashMap<>();
        if (details != null) details.forEach((k, v) -> { if (v != null) safe.put(k, v); });
        return new DomainEvent(UUID.randomUUID().toString(), type, patientId,
                referenceId == null ? null : String.valueOf(referenceId), actor, summary, Instant.now(), safe);
    }

    public String detail(String key) {
        return details == null ? null : details.get(key);
    }
}
