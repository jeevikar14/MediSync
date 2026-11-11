package com.medisync.care.dto;

import com.medisync.care.entity.CareEpisode;
import com.medisync.care.entity.EpisodeStep;
import com.medisync.care.entity.Pathway;
import com.medisync.care.entity.PathwayStep;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CareDtos {
    private CareDtos() {}

    public record StartEpisodeRequest(@NotNull Long patientId, @NotNull Long pathwayId) {}

    public record CloseEpisodeRequest(@Size(max = 500) String notes) {}

    public record PathwayView(Pathway pathway, List<PathwayStep> steps) {}

    public record EpisodeView(CareEpisode episode, List<EpisodeStep> steps) {}

    /** Result of a continuity check. {@code verified} is true only when all three lists are empty. */
    public record VerificationResult(Long episodeId, Long patientId, boolean verified,
                                     List<String> pendingMandatorySteps,
                                     List<String> openCareGaps,
                                     List<String> unacknowledgedHandoffs) {}
}
