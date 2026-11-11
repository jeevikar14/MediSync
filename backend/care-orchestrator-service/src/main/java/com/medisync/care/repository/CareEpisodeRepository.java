package com.medisync.care.repository;

import com.medisync.care.entity.CareEpisode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareEpisodeRepository extends JpaRepository<CareEpisode, Long> {
    List<CareEpisode> findAllByOrderByOpenedAtDesc();
    List<CareEpisode> findByPatientIdOrderByOpenedAtDesc(Long patientId);
    List<CareEpisode> findByPatientIdAndStatus(Long patientId, String status);
    boolean existsByPatientIdAndPathwayIdAndStatus(Long patientId, Long pathwayId, String status);
}
