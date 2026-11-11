package com.medisync.care.service;

import com.medisync.care.entity.TimelineEvent;
import com.medisync.care.repository.TimelineEventRepository;
import com.medisync.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles one incoming domain event: timeline entry -> gap detection -> pathway progress. Idempotent per eventId. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessor {

    private final TimelineEventRepository timeline;
    private final CareGapService gapService;
    private final EpisodeService episodeService;

    @Transactional
    public void process(DomainEvent e) {
        if (e.eventId() == null || e.patientId() == null) {
            log.warn("Ignoring malformed event {}", e);
            return;
        }
        if (timeline.existsByEventId(e.eventId())) {
            log.debug("Duplicate event {} ignored", e.eventId());
            return;
        }
        timeline.save(TimelineEvent.builder().eventId(e.eventId()).patientId(e.patientId()).eventType(e.eventType())
                .referenceId(e.referenceId()).summary(e.summary()).actor(e.actor()).occurredAt(e.occurredAt())
                .details(gapService.toJson(e.details())).build());
        log.info("Timeline <- {} patient={} ref={}", e.eventType(), e.patientId(), e.referenceId());

        gapService.evaluate(e);
        episodeService.advance(e);
    }
}
