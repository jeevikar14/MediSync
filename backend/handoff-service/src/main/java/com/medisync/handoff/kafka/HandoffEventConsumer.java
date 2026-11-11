package com.medisync.handoff.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.handoff.entity.KnownGap;
import com.medisync.handoff.repository.KnownGapRepository;
import com.medisync.handoff.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Turns domain events into notifications and maintains the local care-gap projection. */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandoffEventConsumer {

    private static final String DOCTORS = "ROLE:DOCTOR";

    private final ObjectMapper mapper;
    private final NotificationService notifications;
    private final KnownGapRepository knownGaps;

    @KafkaListener(topics = {Topics.CLINICAL_EVENTS, Topics.APPOINTMENT_EVENTS, Topics.CARE_EVENTS})
    @Transactional
    public void onMessage(String payload) {
        DomainEvent e;
        try {
            e = mapper.readValue(payload, DomainEvent.class);
        } catch (Exception ex) {
            log.error("Skipping unreadable event: {}", payload, ex);
            return;
        }
        log.info("Notification service <- {} patient={}", e.eventType(), e.patientId());
        switch (e.eventType()) {
            case EventTypes.LAB_REPORT_UPLOADED -> notifications.notify(e.detail("orderedBy"), "LAB_REPORT_READY",
                    "Lab report ready for review: " + e.detail("testName") + " (patient #" + e.patientId() + ")"
                            + ("true".equals(e.detail("abnormal")) ? " - ABNORMAL" : ""), e.patientId());
            case EventTypes.APPOINTMENT_MISSED -> notifications.notify(e.detail("doctor"), "APPOINTMENT_MISSED",
                    "Patient #" + e.patientId() + " missed an appointment with you.", e.patientId());
            case EventTypes.CARE_GAP_DETECTED -> {
                upsertGap(e, "OPEN");
                if ("CRITICAL".equals(e.detail("severity"))) {
                    notifications.notify(DOCTORS, "CARE_GAP", "CRITICAL care gap: " + e.summary(), e.patientId());
                }
            }
            case EventTypes.CARE_GAP_ESCALATED -> {
                upsertGap(e, "OPEN");
                notifications.notify(DOCTORS, "CARE_GAP_ESCALATED", "ESCALATED: " + e.summary(), e.patientId());
            }
            case EventTypes.CARE_GAP_RESOLVED -> upsertGap(e, "RESOLVED");
            default -> { /* not interesting for notifications */ }
        }
    }

    private void upsertGap(DomainEvent e, String status) {
        String id = e.detail("gapId");
        if (id == null) return;
        knownGaps.save(KnownGap.builder().gapId(Long.parseLong(id)).patientId(e.patientId()).gapType(e.detail("gapType"))
                .description(e.detail("description")).severity(e.detail("severity")).status(status).build());
    }
}
