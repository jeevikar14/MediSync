package com.medisync.care.repository;

import com.medisync.care.entity.EpisodeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeStepRepository extends JpaRepository<EpisodeStep, Long> {
    List<EpisodeStep> findByEpisodeIdOrderByStepOrder(Long episodeId);
}
