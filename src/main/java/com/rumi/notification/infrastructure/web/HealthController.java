package com.rumi.notification.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Liveness of the service skeleton")
public class HealthController {

    static final String SERVICE_NAME = "rumi-notification-service";

    @Operation(
            summary = "Check that the service is running",
            description = "Skeleton endpoint used to verify deployment and gateway routing. "
                    + "It is not a functional endpoint of the bounded context."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The service is running",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HealthResponse.class),
                    examples = @ExampleObject(
                            name = "Service up",
                            value = """
                                    {
                                      "status": "UP",
                                      "service": "rumi-notification-service"
                                    }"""
                    )
            )
    )
    @GetMapping("/api/v1/alerts/health")
    public HealthResponse health() {
        return new HealthResponse("UP", SERVICE_NAME);
    }
}
