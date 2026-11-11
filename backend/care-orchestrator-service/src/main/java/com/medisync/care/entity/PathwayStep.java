package com.medisync.care.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pathway_steps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PathwayStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pathwayId;
    private int stepOrder;
    private String activityType;
    private String description;
    private boolean mandatory;
}
