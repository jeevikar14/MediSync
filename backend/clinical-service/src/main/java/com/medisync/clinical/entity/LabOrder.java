package com.medisync.clinical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lab_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private String testName;
    private String priority;
    private String orderedBy;
    /** ORDERED -> RESULT_UPLOADED -> REVIEWED */
    private String status;
    private String notes;
    private Instant orderedAt;

    @PrePersist
    void onCreate() { if (orderedAt == null) orderedAt = Instant.now(); }
}
