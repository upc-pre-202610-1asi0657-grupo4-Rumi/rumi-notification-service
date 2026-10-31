package com.rumi.notification.infrastructure.web;

import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.infrastructure.web.dto.ThresholdRequestDto;
import com.rumi.notification.infrastructure.web.dto.ThresholdResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thresholds")
@Tag(name = "Thresholds", description = "Alert limits of each sensor; only administrators reach them through the gateway")
public class ThresholdController {

    private final AlertApplicationService alertService;
    private final ThresholdDtoMapper mapper;

    public ThresholdController(AlertApplicationService alertService, ThresholdDtoMapper mapper) {
        this.alertService = alertService;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Configure the threshold of a sensor",
            description = "Creates the threshold of the sensor or changes its limits (US15). A reading at or above "
                    + "the warning value raises a HIGH alert; at or above the critical value, a CRITICAL alert. "
                    + "The warning value must be lower than the critical value."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Building of the sensor and its limits",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ThresholdRequestDto.class),
                    examples = @ExampleObject(name = "Vibration limits", value = OpenApiExamples.THRESHOLD_REQUEST)
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Threshold created or updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ThresholdResponseDto.class),
                    examples = @ExampleObject(name = "Configured threshold", value = OpenApiExamples.THRESHOLD_RESPONSE)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "A field is missing or not positive, the warning value is not lower than the critical "
                    + "value, the sensor belongs to another building, or sensorId is not a valid UUID",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = {
                            @ExampleObject(name = "Negative value", value = OpenApiExamples.THRESHOLD_VALIDATION_ERROR),
                            @ExampleObject(name = "Warning above critical", value = OpenApiExamples.THRESHOLD_LIMITS_ERROR)
                    }
            )
    )
    @PutMapping("/{sensorId}")
    public ThresholdResponseDto configureThreshold(
            @Parameter(description = "Identifier of the sensor", example = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11")
            @PathVariable UUID sensorId,
            @Valid @RequestBody ThresholdRequestDto request
    ) {
        return mapper.toResponse(alertService.configureThreshold(
                sensorId, request.buildingId(), request.warningValue(), request.criticalValue()));
    }

    @Operation(summary = "Get the threshold of a sensor", description = "Returns the alert limits of the sensor (US15).")
    @ApiResponse(
            responseCode = "200",
            description = "Threshold of the sensor",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ThresholdResponseDto.class),
                    examples = @ExampleObject(name = "Configured threshold", value = OpenApiExamples.THRESHOLD_RESPONSE)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sensor has no threshold",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "No threshold", value = OpenApiExamples.THRESHOLD_NOT_FOUND_ERROR)
            )
    )
    @GetMapping("/{sensorId}")
    public ThresholdResponseDto getThreshold(
            @Parameter(description = "Identifier of the sensor", example = "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11")
            @PathVariable UUID sensorId
    ) {
        return mapper.toResponse(alertService.getThreshold(sensorId));
    }

    @Operation(
            summary = "List the thresholds of a building",
            description = "Returns the thresholds of the sensors of the building (US15). "
                    + "The list is empty when no sensor has a threshold yet."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Thresholds of the building (possibly none)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ThresholdResponseDto.class)),
                    examples = @ExampleObject(name = "One threshold", value = OpenApiExamples.THRESHOLD_LIST_RESPONSE)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "buildingId is missing or is not a valid UUID",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @GetMapping
    public List<ThresholdResponseDto> listThresholds(
            @Parameter(description = "Identifier of the building", required = true,
                    example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d")
            @RequestParam UUID buildingId
    ) {
        return alertService.listThresholds(buildingId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
