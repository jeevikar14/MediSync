package com.medisync.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AppointmentDtos {
    private AppointmentDtos() {}

    public record BookRequest(@NotNull Long patientId, @NotBlank String doctorName, @NotBlank String department,
                              @NotNull Instant scheduledAt, @Size(max = 255) String reason, boolean followUp) {}

    public record NotesRequest(@Size(max = 500) String notes) {}
}
