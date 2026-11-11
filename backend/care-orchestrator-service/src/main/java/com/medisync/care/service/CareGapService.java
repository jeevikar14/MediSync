package com.medisync.care.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.care.entity.CareGap;
import com.medisync.care.entity.TimelineEvent;
import com.medisync.care.repository.CareGapRepository;
import com.medisync.care.repository.TimelineEventRepository;
import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.common.kafka.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Care Gap Detection.
 *
 * <ul>
 *   <li>LAB_REPORT_UNREVIEWED - opened on LAB_REPORT_UPLOADED, resolved on LAB_REPORT_REVIEWED</li>
 *   <li>MISSED_FOLLOWUP       - opened on APPOINTMENT_MISSED (follow-up only), resolved when the patient is re-booked</li>
 *   <li>COMBINED              - CRITICAL gap while a patient has BOTH of the above open</li>
 *   <li>Escalation            - a scheduled job raises gaps that stay open too long to CRITICAL</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareGapService {

    public static final String LAB_UNREVIEWED = "LAB_REPORT_UNREVIEWED";
    public static final String MISSED_FOLLOWUP = "MISSED_FOLLOWUP";
    public static final String COMBINED = "COMBINED";

    private final CareGapRepository gaps;
    private final TimelineEventRepository timeline;
    private final DomainEventPublisher publisher;
    private final ObjectMapper mapper;

    @Value("${care.gap.escalation-minutes}")
    private long escalationMinutes;

    /** Apply gap rules for one incoming event, then reconcile the COMBINED gap. Called inside the event transaction. */
    public void evaluate(DomainEvent e) {
        Long pid = e.patientId();
        switch (e.eventType()) {
            case EventTypes.LAB_REPORT_UPLOADED -> open(pid, LAB_UNREVIEWED, e.referenceId(),
                    "Lab report '" + e.detail("testName") + "' uploaded but not yet reviewed",
                    "true".equals(e.detail("abnormal")) ? "HIGH" : "MEDIUM");
            case EventTypes.LAB_REPORT_REVIEWED -> resolve(pid, LAB_UNREVIEWED, e.referenceId(), "Reviewed by " + e.actor());
            case EventTypes.APPOINTMENT_MISSED -> {
                if ("true".equals(e.detail("followUp"))) {
                    open(pid, MISSED_FOLLOWUP, e.referenceId(),
                            "Missed follow-up appointment with " + e.detail("doctor"), "HIGH");
                }
            }
            case EventTypes.APPOINTMENT_BOOKED -> resolve(pid, MISSED_FOLLOWUP, null, "Patient re-booked an appointment");
            default -> { return; }
        }
        reconcileCombined(pid);
    }

    private void reconcileCombined(Long pid) {
        boolean lab = !gaps.findByPatientIdAndGapTypeAndStatus(pid, LAB_UNREVIEWED, "OPEN").isEmpty();
        boolean missed = !gaps.findByPatientIdAndGapTypeAndStatus(pid, MISSED_FOLLOWUP, "OPEN").isEmpty();
        boolean combinedOpen = !gaps.findByPatientIdAndGapTypeAndStatus(pid, COMBINED, "OPEN").isEmpty();
        if (lab && missed && !combinedOpen) {
            open(pid, COMBINED, "patient-" + pid,
                    "Unreviewed lab report AND missed follow-up - patient at high risk of being lost to care", "CRITICAL");
        } else if (!(lab && missed) && combinedOpen) {
            resolve(pid, COMBINED, null, "One of the combined conditions was resolved");
        }
    }

    private void open(Long pid, String type, String ref, String description, String severity) {
        boolean exists = gaps.findByPatientIdAndGapTypeAndStatus(pid, type, "OPEN").stream()
                .anyMatch(g -> java.util.Objects.equals(g.getReferenceId(), ref));
        if (exists) return;
        CareGap gap = gaps.save(CareGap.builder().patientId(pid).gapType(type).referenceId(ref)
                .description(description).severity(severity).status("OPEN").detectedAt(Instant.now()).build());
        log.info("Care gap DETECTED id={} type={} patient={}", gap.getId(), type, pid);
        recordTimeline(pid, EventTypes.CARE_GAP_DETECTED, String.valueOf(gap.getId()), "CARE GAP detected: " + description, "care-orchestrator");
        publisher.publish(Topics.CARE_EVENTS, DomainEvent.of(EventTypes.CARE_GAP_DETECTED, pid, gap.getId(), "care-orchestrator",
                description, gapDetails(gap)));
    }

    /** Resolve open gaps of a type for a patient; ref == null resolves all of that type. */
    private void resolve(Long pid, String type, String ref, String note) {
        for (CareGap gap : gaps.findByPatientIdAndGapTypeAndStatus(pid, type, "OPEN")) {
            if (ref != null && !ref.equals(gap.getReferenceId())) continue;
            gap.setStatus("RESOLVED");
            gap.setResolvedAt(Instant.now());
            gap.setResolutionNote(note);
            gaps.save(gap);
            log.info("Care gap RESOLVED id={} type={} patient={}", gap.getId(), type, pid);
            recordTimeline(pid, EventTypes.CARE_GAP_RESOLVED, String.valueOf(gap.getId()), "Care gap resolved: " + gap.getDescription(), "care-orchestrator");
            publisher.publish(Topics.CARE_EVENTS, DomainEvent.of(EventTypes.CARE_GAP_RESOLVED, pid, gap.getId(), "care-orchestrator",
                    "Resolved: " + gap.getDescription(), gapDetails(gap)));
        }
    }

    /** Time based detection: gaps that stay open too long become CRITICAL. */
    @Scheduled(fixedDelayString = "${care.gap.scan-interval-ms}", initialDelay = 30000)
    @Transactional
    public void escalateOverdueGaps() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(escalationMinutes));
        List<CareGap> candidates = gaps.findByStatusAndEscalatedFalse("OPEN");
        for (CareGap gap : candidates) {
            if (COMBINED.equals(gap.getGapType()) || !gap.getDetectedAt().isBefore(cutoff)) continue;
            gap.setEscalated(true);
            gap.setSeverity("CRITICAL");
            gap.setDescription(gap.getDescription() + " [OVERDUE]");
            gaps.save(gap);
            log.warn("Care gap ESCALATED id={} patient={}", gap.getId(), gap.getPatientId());
            recordTimeline(gap.getPatientId(), EventTypes.CARE_GAP_ESCALATED, String.valueOf(gap.getId()),
                    "Care gap ESCALATED (overdue): " + gap.getDescription(), "care-orchestrator");
            publisher.publish(Topics.CARE_EVENTS, DomainEvent.of(EventTypes.CARE_GAP_ESCALATED, gap.getPatientId(), gap.getId(),
                    "care-orchestrator", "Overdue: " + gap.getDescription(), gapDetails(gap)));
        }
    }

    @Transactional(readOnly = true)
    public List<CareGap> list(Long patientId, String status) {
        List<CareGap> all = patientId == null ? gaps.findAllByOrderByDetectedAtDesc() : gaps.findByPatientIdOrderByDetectedAtDesc(patientId);
        return (status == null || status.isBlank()) ? all : all.stream().filter(g -> g.getStatus().equalsIgnoreCase(status)).toList();
    }

    // ---------- timeline helper (shared) ----------
    public void recordTimeline(Long pid, String type, String ref, String summary, String actor) {
        timeline.save(TimelineEvent.builder().eventId(UUID.randomUUID().toString()).patientId(pid).eventType(type)
                .referenceId(ref).summary(summary).actor(actor).occurredAt(Instant.now()).build());
    }

    public String toJson(Map<String, String> details) {
        try { return mapper.writeValueAsString(details); } catch (JsonProcessingException ex) { return null; }
    }

    private Map<String, String> gapDetails(CareGap g) {
        return Map.of("gapId", String.valueOf(g.getId()), "gapType", g.getGapType(),
                "severity", g.getSeverity(), "description", g.getDescription());
    }
}
