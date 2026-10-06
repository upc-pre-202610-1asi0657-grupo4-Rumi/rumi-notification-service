package com.rumi.notification.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HealthResponse", description = "Liveness information of the service")
public record HealthResponse(
        @Schema(description = "Liveness status", example = "UP")
        String status,
        @Schema(description = "Name of the service", example = "rumi-notification-service")
        String service
) {
}
