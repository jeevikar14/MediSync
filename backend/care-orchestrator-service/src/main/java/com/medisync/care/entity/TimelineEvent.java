package com.medisync.care.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "timeline_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimelineEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    private Long patientId;
    private String eventType;
    private String referenceId;
    private String summary;
    private String actor;
    private Instant occurredAt;
    @Column(columnDefinition = "TEXT")
    private String details;
}
