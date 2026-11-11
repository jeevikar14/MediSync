package com.medisync.handoff.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "handoffs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Handoff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private String fromDoctor;
    private String toDoctor;
    private String fromDepartment;
    private String toDepartment;
    private String reason;
    @Column(columnDefinition = "TEXT")
    private String clinicalSummary;
    /** PENDING -> ACKNOWLEDGED */
    private String status;
    private Instant createdAt;
    private Instant acknowledgedAt;
    private String acknowledgedBy;
    private String ackComment;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
}
