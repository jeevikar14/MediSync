package com.medisync.clinical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lab_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long labOrderId;
    private Long patientId;
    private String testName;
    private String resultSummary;
    private boolean abnormal;
    private String uploadedBy;
    private Instant uploadedAt;
    private boolean reviewed;
    private String reviewedBy;
    private Instant reviewedAt;
    private String reviewComment;

    @PrePersist
    void onCreate() { if (uploadedAt == null) uploadedAt = Instant.now(); }
}
