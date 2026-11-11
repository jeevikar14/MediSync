package com.medisync.care.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.care.service.EventProcessor;
import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Listens to every business topic (not its own care topic) and feeds the orchestrator. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CareEventConsumer {

    private final ObjectMapper mapper;
    private final EventProcessor processor;

    @KafkaListener(topics = {Topics.PATIENT_EVENTS, Topics.CLINICAL_EVENTS, Topics.APPOINTMENT_EVENTS, Topics.HANDOFF_EVENTS})
    public void onMessage(String payload) {
        DomainEvent event;
        try {
            event = mapper.readValue(payload, DomainEvent.class);
        } catch (Exception ex) {
            log.error("Skipping unreadable event: {}", payload, ex);
            return;
        }
        processor.process(event);
    }
}
