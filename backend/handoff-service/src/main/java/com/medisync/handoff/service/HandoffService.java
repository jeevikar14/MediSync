package com.medisync.handoff.service;

import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.common.exception.BusinessRuleException;
import com.medisync.common.exception.ForbiddenException;
import com.medisync.common.exception.ResourceNotFoundException;
import com.medisync.common.kafka.DomainEventPublisher;
import com.medisync.handoff.dto.HandoffDtos.*;
import com.medisync.handoff.entity.Handoff;
import com.medisync.handoff.entity.HandoffItem;
import com.medisync.handoff.entity.KnownGap;
import com.medisync.handoff.repository.HandoffItemRepository;
import com.medisync.handoff.repository.HandoffRepository;
import com.medisync.handoff.repository.KnownGapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HandoffService {

    private final HandoffRepository handoffs;
    private final HandoffItemRepository items;
    private final KnownGapRepository knownGaps;
    private final NotificationService notifications;
    private final DomainEventPublisher publisher;

    @Transactional
    public HandoffView create(HandoffRequest req, String actor) {
        if (req.toDoctor().equals(actor)) {
            throw new BusinessRuleException("Cannot hand off a patient to yourself");
        }
        Handoff h = handoffs.save(Handoff.builder().patientId(req.patientId()).fromDoctor(actor).toDoctor(req.toDoctor())
                .fromDepartment(req.fromDepartment()).toDepartment(req.toDepartment()).reason(req.reason())
                .clinicalSummary(req.clinicalSummary()).status("PENDING").build());

        List<HandoffItem> saved = new ArrayList<>();
        Set<String> seenRefs = new HashSet<>();
        if (req.items() != null) {
            for (ItemRequest i : req.items()) {
                if (i.referenceId() != null) seenRefs.add(i.itemType() + ":" + i.referenceId());
                saved.add(items.save(HandoffItem.builder().handoffId(h.getId()).itemType(i.itemType().toUpperCase())
                        .description(i.description()).referenceId(i.referenceId()).build()));
            }
        }
        if (req.includeOpenGaps() == null || req.includeOpenGaps()) {
            for (KnownGap g : suggestedGaps(req.patientId())) {
                if (seenRefs.contains("CARE_GAP:" + g.getGapId())) continue;
                saved.add(items.save(HandoffItem.builder().handoffId(h.getId()).itemType("CARE_GAP")
                        .description("[" + g.getSeverity() + "] " + g.getDescription()).referenceId(String.valueOf(g.getGapId())).build()));
            }
        }

        publisher.publish(Topics.HANDOFF_EVENTS, DomainEvent.of(EventTypes.HANDOFF_CREATED, h.getPatientId(), h.getId(), actor,
                "Handoff #" + h.getId() + " created: " + actor + " -> " + h.getToDoctor() + " (" + saved.size() + " pending items)",
                Map.of("fromDoctor", actor, "toDoctor", h.getToDoctor(), "itemCount", String.valueOf(saved.size()))));
        notifications.notify(h.getToDoctor(), "HANDOFF_RECEIVED",
                "New handoff from " + actor + " for patient #" + h.getPatientId() + " with " + saved.size()
                        + " pending item(s). Your acknowledgement is required.", h.getPatientId());
        return new HandoffView(h, saved);
    }

    @Transactional
    public HandoffView acknowledge(Long id, String comment, String actor, String role) {
        Handoff h = handoffs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Handoff " + id + " not found"));
        if (!"PENDING".equals(h.getStatus())) {
            throw new BusinessRuleException("Handoff " + id + " is already " + h.getStatus());
        }
        if (!h.getToDoctor().equals(actor) && !"ADMIN".equals(role)) {
            throw new ForbiddenException("Only the receiving doctor (" + h.getToDoctor() + ") can acknowledge this handoff");
        }
        h.setStatus("ACKNOWLEDGED");
        h.setAcknowledgedAt(Instant.now());
        h.setAcknowledgedBy(actor);
        h.setAckComment(comment);
        h = handoffs.save(h);

        publisher.publish(Topics.HANDOFF_EVENTS, DomainEvent.of(EventTypes.HANDOFF_ACKNOWLEDGED, h.getPatientId(), h.getId(), actor,
                "Handoff #" + h.getId() + " acknowledged by " + actor,
                Map.of("fromDoctor", h.getFromDoctor(), "toDoctor", h.getToDoctor())));
        notifications.notify(h.getFromDoctor(), "HANDOFF_ACKNOWLEDGED",
                "Handoff #" + h.getId() + " for patient #" + h.getPatientId() + " was acknowledged by " + actor + ".", h.getPatientId());
        return view(h);
    }

    @Transactional(readOnly = true)
    public HandoffView get(Long id) {
        return view(handoffs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Handoff " + id + " not found")));
    }

    @Transactional(readOnly = true)
    public List<HandoffView> list(Long patientId, String status, String toDoctor) {
        List<Handoff> all = patientId == null ? handoffs.findAllByOrderByCreatedAtDesc() : handoffs.findByPatientIdOrderByCreatedAtDesc(patientId);
        return all.stream()
                .filter(h -> status == null || status.isBlank() || h.getStatus().equalsIgnoreCase(status))
                .filter(h -> toDoctor == null || toDoctor.isBlank() || h.getToDoctor().equals(toDoctor))
                .map(this::view).toList();
    }

    /** The "intelligent" part: open care gaps of the patient, offered as pending handoff items. */
    @Transactional(readOnly = true)
    public List<KnownGap> suggestedGaps(Long patientId) {
        return knownGaps.findByPatientIdAndStatus(patientId, "OPEN");
    }

    private HandoffView view(Handoff h) {
        return new HandoffView(h, items.findByHandoffId(h.getId()));
    }
}
