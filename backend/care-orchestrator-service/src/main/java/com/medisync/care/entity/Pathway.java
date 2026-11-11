package com.medisync.care.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pathways")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pathway {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String description;
}
