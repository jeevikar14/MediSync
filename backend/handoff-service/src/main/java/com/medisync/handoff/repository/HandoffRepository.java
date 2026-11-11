package com.medisync.handoff.repository;

import com.medisync.handoff.entity.Handoff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoffRepository extends JpaRepository<Handoff, Long> {
    List<Handoff> findAllByOrderByCreatedAtDesc();
    List<Handoff> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
