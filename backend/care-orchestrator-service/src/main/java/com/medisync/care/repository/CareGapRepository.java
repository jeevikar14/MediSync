package com.medisync.care.repository;

import com.medisync.care.entity.CareGap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareGapRepository extends JpaRepository<CareGap, Long> {
    List<CareGap> findAllByOrderByDetectedAtDesc();
    List<CareGap> findByPatientIdOrderByDetectedAtDesc(Long patientId);
    List<CareGap> findByPatientIdAndStatus(Long patientId, String status);
    List<CareGap> findByPatientIdAndGapTypeAndStatus(Long patientId, String gapType, String status);
    List<CareGap> findByStatusAndEscalatedFalse(String status);
}
