package com.medisync.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ClinicalDtos {
    private ClinicalDtos() {}

    public record LabOrderRequest(@NotNull Long patientId, @NotBlank @Size(max = 120) String testName,
                                  String priority, @Size(max = 500) String notes) {}

    public record LabReportRequest(@NotNull Long labOrderId, @NotBlank @Size(max = 1000) String resultSummary,
                                   boolean abnormal) {}

    public record ReviewRequest(@Size(max = 500) String comment) {}

    public record NoteRequest(@NotNull Long patientId, @NotBlank String noteType, @NotBlank String content) {}
}
