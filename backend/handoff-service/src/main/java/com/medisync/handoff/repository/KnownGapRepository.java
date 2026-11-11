package com.medisync.handoff.repository;

import com.medisync.handoff.entity.KnownGap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnownGapRepository extends JpaRepository<KnownGap, Long> {
    List<KnownGap> findByPatientIdAndStatus(Long patientId, String status);
}
