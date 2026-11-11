package com.medisync.care.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "care_gaps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareGap {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    /** LAB_REPORT_UNREVIEWED | MISSED_FOLLOWUP | COMBINED */
    private String gapType;
    private String referenceId;
    private String description;
    /** MEDIUM | HIGH | CRITICAL */
    private String severity;
    /** OPEN | RESOLVED */
    private String status;
    private boolean escalated;
    private Instant detectedAt;
    private Instant resolvedAt;
    private String resolutionNote;
}
