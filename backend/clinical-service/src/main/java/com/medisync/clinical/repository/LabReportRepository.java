package com.medisync.clinical.repository;

import com.medisync.clinical.entity.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findAllByOrderByUploadedAtDesc();
    List<LabReport> findByPatientIdOrderByUploadedAtDesc(Long patientId);
}
