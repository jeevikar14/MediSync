package com.medisync.clinical.repository;

import com.medisync.clinical.entity.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    List<LabOrder> findAllByOrderByOrderedAtDesc();
    List<LabOrder> findByPatientIdOrderByOrderedAtDesc(Long patientId);
}
