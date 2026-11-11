package com.medisync.handoff.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "handoff_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HandoffItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long handoffId;
    /** CARE_GAP | LAB_REVIEW | FOLLOW_UP | OTHER */
    private String itemType;
    private String description;
    private String referenceId;
}
