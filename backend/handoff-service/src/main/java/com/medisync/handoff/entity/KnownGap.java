package com.medisync.handoff.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "known_gaps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnownGap {
    @Id
    private Long gapId;
    private Long patientId;
    private String gapType;
    private String description;
    private String severity;
    private String status;
}
