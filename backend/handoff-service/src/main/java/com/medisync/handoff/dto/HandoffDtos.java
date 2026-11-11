package com.medisync.handoff.dto;

import com.medisync.handoff.entity.Handoff;
import com.medisync.handoff.entity.HandoffItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class HandoffDtos {
    private HandoffDtos() {}

    public record ItemRequest(@NotBlank String itemType, @NotBlank @Size(max = 300) String description, String referenceId) {}

    /** includeOpenGaps (default true) = "intelligent" mode: open care gaps of the patient are added as pending items. */
    public record HandoffRequest(@NotNull Long patientId, @NotBlank String toDoctor, String fromDepartment,
                                 String toDepartment, @Size(max = 255) String reason,
                                 @NotBlank String clinicalSummary, Boolean includeOpenGaps,
                                 @Valid List<ItemRequest> items) {}

    public record AcknowledgeRequest(@Size(max = 500) String comment) {}

    public record HandoffView(Handoff handoff, List<HandoffItem> items) {}
}
