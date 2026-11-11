package com.medisync.patient.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public final class PatientDtos {
    private PatientDtos() {}

    public record PatientRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotNull @Past LocalDate dateOfBirth,
            @NotBlank @Size(max = 20) String gender,
            @Size(max = 20) String phone,
            @Email @Size(max = 120) String email,
            @Size(max = 255) String address,
            @Size(max = 5) String bloodGroup) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String token, String username, String fullName, String role) {}

    public record UserSummary(String username, String fullName, String role) {}
}
