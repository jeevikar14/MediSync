package com.medisync.care.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "episode_steps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EpisodeStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long episodeId;
    private int stepOrder;
    private String activityType;
    private String description;
    private boolean mandatory;
    /** PENDING | COMPLETED */
    private String status;
    private Instant completedAt;
    private String completedBy;
    private String evidenceRef;
}
