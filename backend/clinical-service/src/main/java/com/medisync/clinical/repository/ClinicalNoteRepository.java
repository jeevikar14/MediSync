package com.medisync.clinical.repository;

import com.medisync.clinical.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {
    List<ClinicalNote> findAllByOrderByCreatedAtDesc();
    List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
