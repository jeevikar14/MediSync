package com.medisync.handoff.repository;

import com.medisync.handoff.entity.HandoffItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoffItemRepository extends JpaRepository<HandoffItem, Long> {
    List<HandoffItem> findByHandoffId(Long handoffId);
}
