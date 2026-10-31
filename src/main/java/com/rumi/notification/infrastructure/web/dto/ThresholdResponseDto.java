package com.rumi.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "ThresholdResponse", description = "Alert limits configured for a sensor")
public record ThresholdResponseDto(
        @Schema(description = "Sensor the threshold applies to", example = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11")
        UUID sensorId,
        @Schema(description = "Building where the sensor is installed", example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d")
        UUID buildingId,
        @Schema(description = "A reading at or above this value raises a HIGH alert", example = "0.05")
        double warningValue,
        @Schema(description = "A reading at or above this value raises a CRITICAL alert", example = "0.10")
        double criticalValue,
        @Schema(description = "Last change of the limits (UTC)", example = "2026-10-30T15:00:00Z")
        Instant updatedAt
) {
}
