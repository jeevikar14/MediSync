package com.medisync.common.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes {@link DomainEvent}s to Kafka as JSON strings, keyed by patientId (keeps per-patient ordering). */
@Slf4j
@Component
public class DomainEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public DomainEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public void publish(String topic, DomainEvent event) {
        try {
            String json = mapper.writeValueAsString(event);
            kafka.send(topic, String.valueOf(event.patientId()), json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish {} to {}", event.eventType(), topic, ex);
                } else {
                    log.info("Published {} (patient={}) to {}", event.eventType(), event.patientId(), topic);
                }
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialise event " + event.eventType(), e);
        }
    }
}
