package com.rumi.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "RiskSimulationRequest",
        description = "Risk index to simulate, as Seismic Correlation would publish it (dev profile only)")
public record RiskSimulationRequestDto(
        @Schema(description = "Building whose risk was calculated", example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID buildingId,
        @Schema(description = "Risk index id; a new one is generated when omitted",
                example = "4d2b8f6e-1a3c-4e5d-9f7b-2c4a6e8d0f13", nullable = true)
        UUID riskIndexId,
        @Schema(description = "Seismic event id; a new one is generated when omitted",
                example = "9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b", nullable = true)
        UUID seismicEventId,
        @Schema(description = "LOW, MEDIUM, HIGH or CRITICAL", example = "CRITICAL",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String riskLevel
) {
}
