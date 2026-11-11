package com.medisync.care.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "care_episodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareEpisode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private Long pathwayId;
    private String pathwayName;
    /** ACTIVE | CLOSED */
    private String status;
    private String openedBy;
    private Instant openedAt;
    private Instant closedAt;
    private String closingNotes;
}
