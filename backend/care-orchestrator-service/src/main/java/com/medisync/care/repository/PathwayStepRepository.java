package com.medisync.care.repository;

import com.medisync.care.entity.PathwayStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathwayStepRepository extends JpaRepository<PathwayStep, Long> {
    List<PathwayStep> findByPathwayIdOrderByStepOrder(Long pathwayId);
}
