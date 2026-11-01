package com.rumi.notification.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "ReadingSimulationRequest",
        description = "Sensor reading to compare with its threshold, as Structural Monitoring would publish it "
                + "(dev profile only)")
public record ReadingSimulationRequestDto(
        @Schema(description = "Sensor of the reading", example = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID sensorId,
        @Schema(description = "Building of the sensor", example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID buildingId,
        @Schema(description = "Zone of the sensor", example = "FLOOR-3-NORTH", maxLength = 50, nullable = true)
        @Size(max = 50)
        String zone,
        @Schema(description = "Measured value", example = "0.12", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Double value
) {
}
