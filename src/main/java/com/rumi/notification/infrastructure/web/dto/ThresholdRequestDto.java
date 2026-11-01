package com.rumi.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(name = "ThresholdRequest", description = "Alert limits of a sensor")
public record ThresholdRequestDto(
        @Schema(description = "Building where the sensor is installed",
                example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID buildingId,
        @Schema(description = "A reading at or above this value raises a HIGH alert", example = "0.05",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        Double warningValue,
        @Schema(description = "A reading at or above this value raises a CRITICAL alert; it must be greater "
                + "than the warning value", example = "0.10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        Double criticalValue
) {
}
