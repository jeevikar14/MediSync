package com.medisync.handoff.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** a username, or ROLE:DOCTOR style broadcast address */
    private String recipient;
    private String type;
    private String message;
    private Long patientId;
    @Column(name = "is_read")
    private boolean read;
    private Instant createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
}
