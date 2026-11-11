package com.medisync.care.service;

import com.medisync.care.dto.CareDtos.*;
import com.medisync.care.entity.*;
import com.medisync.care.repository.*;
import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.common.exception.BusinessRuleException;
import com.medisync.common.exception.ResourceNotFoundException;
import com.medisync.common.kafka.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Clinical pathways, care episodes and Continuity Verification. */
@Service
@RequiredArgsConstructor
public class EpisodeService {

    private final PathwayRepository pathways;
    private final PathwayStepRepository pathwaySteps;
    private final CareEpisodeRepository episodes;
    private final EpisodeStepRepository episodeSteps;
    private final CareGapRepository gaps;
    private final TimelineEventRepository timeline;
    private final CareGapService gapService;
    private final DomainEventPublisher publisher;

    @Transactional(readOnly = true)
    public List<PathwayView> listPathways() {
        return pathways.findAll().stream().map(p -> new PathwayView(p, pathwaySteps.findByPathwayIdOrderByStepOrder(p.getId()))).toList();
    }

    @Transactional
    public EpisodeView start(Long patientId, Long pathwayId, String actor) {
        Pathway pathway = pathways.findById(pathwayId).orElseThrow(() -> new ResourceNotFoundException("Pathway " + pathwayId + " not found"));
        if (episodes.existsByPatientIdAndPathwayIdAndStatus(patientId, pathwayId, "ACTIVE")) {
            throw new BusinessRuleException("Patient already has an active episode for pathway '" + pathway.getName() + "'");
        }
        CareEpisode ep = episodes.save(CareEpisode.builder().patientId(patientId).pathwayId(pathwayId)
                .pathwayName(pathway.getName()).status("ACTIVE").openedBy(actor).openedAt(Instant.now()).build());
        List<EpisodeStep> steps = pathwaySteps.findByPathwayIdOrderByStepOrder(pathwayId).stream()
                .map(s -> EpisodeStep.builder().episodeId(ep.getId()).stepOrder(s.getStepOrder()).activityType(s.getActivityType())
                        .description(s.getDescription()).mandatory(s.isMandatory()).status("PENDING").build())
                .toList();
        episodeSteps.saveAll(steps);
        gapService.recordTimeline(patientId, EventTypes.EPISODE_STARTED, String.valueOf(ep.getId()),
                "Care pathway started: " + pathway.getName(), actor);
        return view(ep);
    }

    @Transactional(readOnly = true)
    public List<EpisodeView> list(Long patientId) {
        List<CareEpisode> list = patientId == null ? episodes.findAllByOrderByOpenedAtDesc() : episodes.findByPatientIdOrderByOpenedAtDesc(patientId);
        return list.stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public EpisodeView get(Long id) { return view(episode(id)); }

    /** Event driven progress: an event completes the first matching pending step of every active episode of the patient. */
    @Transactional
    public void advance(DomainEvent e) {
        for (CareEpisode ep : episodes.findByPatientIdAndStatus(e.patientId(), "ACTIVE")) {
            if (e.occurredAt() != null && e.occurredAt().isBefore(ep.getOpenedAt())) continue;
            episodeSteps.findByEpisodeIdOrderByStepOrder(ep.getId()).stream()
                    .filter(s -> "PENDING".equals(s.getStatus()) && s.getActivityType().equals(e.eventType()))
                    .findFirst()
                    .ifPresent(s -> complete(s, e.actor(), e.eventType() + ":" + e.referenceId()));
        }
    }

    @Transactional
    public EpisodeView completeStepManually(Long stepId, String actor) {
        EpisodeStep step = episodeSteps.findById(stepId).orElseThrow(() -> new ResourceNotFoundException("Step " + stepId + " not found"));
        CareEpisode ep = episode(step.getEpisodeId());
        if (!"ACTIVE".equals(ep.getStatus())) throw new BusinessRuleException("Episode is closed");
        if (!"PENDING".equals(step.getStatus())) throw new BusinessRuleException("Step is already completed");
        complete(step, actor, "MANUAL");
        return view(ep);
    }

    /** Continuity Verification: may this episode be closed? */
    @Transactional(readOnly = true)
    public VerificationResult verify(Long episodeId) {
        CareEpisode ep = episode(episodeId);
        List<String> pending = episodeSteps.findByEpisodeIdOrderByStepOrder(episodeId).stream()
                .filter(s -> s.isMandatory() && "PENDING".equals(s.getStatus()))
                .map(s -> s.getStepOrder() + ". " + s.getDescription()).toList();
        List<String> openGaps = gaps.findByPatientIdAndStatus(ep.getPatientId(), "OPEN").stream()
                .map(g -> "[" + g.getSeverity() + "] " + g.getDescription()).toList();

        Set<String> acknowledged = new HashSet<>();
        timeline.findByPatientIdAndEventType(ep.getPatientId(), EventTypes.HANDOFF_ACKNOWLEDGED)
                .forEach(t -> acknowledged.add(t.getReferenceId()));
        List<String> unacked = timeline.findByPatientIdAndEventType(ep.getPatientId(), EventTypes.HANDOFF_CREATED).stream()
                .filter(t -> !acknowledged.contains(t.getReferenceId()))
                .map(t -> "Handoff #" + t.getReferenceId() + " awaiting acknowledgement - " + t.getSummary()).toList();

        boolean ok = pending.isEmpty() && openGaps.isEmpty() && unacked.isEmpty();
        return new VerificationResult(episodeId, ep.getPatientId(), ok, pending, openGaps, unacked);
    }

    /** Closes the episode only if verification passes; the (possibly failing) result is always returned. */
    @Transactional
    public VerificationResult close(Long episodeId, String notes, String actor) {
        CareEpisode ep = episode(episodeId);
        if (!"ACTIVE".equals(ep.getStatus())) throw new BusinessRuleException("Episode is already closed");
        VerificationResult result = verify(episodeId);
        if (result.verified()) {
            ep.setStatus("CLOSED");
            ep.setClosedAt(Instant.now());
            ep.setClosingNotes(notes);
            episodes.save(ep);
            gapService.recordTimeline(ep.getPatientId(), EventTypes.EPISODE_CLOSED, String.valueOf(ep.getId()),
                    "Care episode closed after continuity verification: " + ep.getPathwayName(), actor);
            publisher.publish(Topics.CARE_EVENTS, DomainEvent.of(EventTypes.EPISODE_CLOSED, ep.getPatientId(), ep.getId(), actor,
                    "Care episode closed: " + ep.getPathwayName(), Map.of("pathway", ep.getPathwayName())));
        }
        return result;
    }

    private void complete(EpisodeStep step, String actor, String evidence) {
        step.setStatus("COMPLETED");
        step.setCompletedAt(Instant.now());
        step.setCompletedBy(actor);
        step.setEvidenceRef(evidence);
        episodeSteps.save(step);
    }

    private CareEpisode episode(Long id) {
        return episodes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Episode " + id + " not found"));
    }

    private EpisodeView view(CareEpisode ep) {
        return new EpisodeView(ep, episodeSteps.findByEpisodeIdOrderByStepOrder(ep.getId()));
    }
}
