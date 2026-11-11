package com.medisync.common.kafka;

import com.medisync.common.event.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Every service declares the topics, so they exist regardless of start-up order. */
@Configuration
public class KafkaTopicConfig {
    @Bean NewTopic patientEventsTopic()     { return TopicBuilder.name(Topics.PATIENT_EVENTS).partitions(1).replicas(1).build(); }
    @Bean NewTopic clinicalEventsTopic()    { return TopicBuilder.name(Topics.CLINICAL_EVENTS).partitions(1).replicas(1).build(); }
    @Bean NewTopic appointmentEventsTopic() { return TopicBuilder.name(Topics.APPOINTMENT_EVENTS).partitions(1).replicas(1).build(); }
    @Bean NewTopic careEventsTopic()        { return TopicBuilder.name(Topics.CARE_EVENTS).partitions(1).replicas(1).build(); }
    @Bean NewTopic handoffEventsTopic()     { return TopicBuilder.name(Topics.HANDOFF_EVENTS).partitions(1).replicas(1).build(); }
}
