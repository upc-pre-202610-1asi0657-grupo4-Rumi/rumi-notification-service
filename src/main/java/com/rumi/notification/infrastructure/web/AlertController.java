package com.rumi.notification.infrastructure.web;

import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.infrastructure.web.dto.AlertResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Alerts raised by seismic risk or by sensor readings that exceed a threshold")
public class AlertController {

    private final AlertApplicationService alertService;
    private final AlertDtoMapper mapper;

    public AlertController(AlertApplicationService alertService, AlertDtoMapper mapper) {
        this.alertService = alertService;
        this.mapper = mapper;
    }

    @Operation(
            summary = "List the alerts of a building",
            description = "Returns the alerts of the building, newest first (US14, US15, US16). "
                    + "Filter by status to get, for example, only the OPEN alerts. "
                    + "Through the gateway, a RESIDENT only sees the alerts of the buildings in their token."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Alerts of the building (possibly none)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AlertResponseDto.class)),
                    examples = @ExampleObject(name = "One open alert", value = OpenApiExamples.ALERT_LIST_RESPONSE)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "buildingId is missing or is not a valid UUID, or status is not OPEN, ACKNOWLEDGED "
                    + "or RESOLVED",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = {
                            @ExampleObject(name = "Missing building id",
                                    value = OpenApiExamples.MISSING_BUILDING_ID_ERROR),
                            @ExampleObject(name = "Unknown status", value = OpenApiExamples.INVALID_STATUS_ERROR)
                    }
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "A resident asked for the alerts of a building that is not in their token",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Other building",
                            value = OpenApiExamples.RESIDENT_FORBIDDEN_ERROR)
            )
    )
    @GetMapping
    public List<AlertResponseDto> listAlerts(
            @Parameter(description = "Identifier of the building", required = true,
                    example = "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d")
            @RequestParam UUID buildingId,
            @Parameter(description = "Only alerts with this status", example = "OPEN")
            @RequestParam(required = false) AlertStatus status,
            @Parameter(hidden = true) @RequestHeader(value = GatewayUser.ROLE_HEADER, required = false) String role,
            @Parameter(hidden = true) @RequestHeader(value = GatewayUser.BUILDING_IDS_HEADER, required = false)
            String buildingIds
    ) {
        GatewayUser.from(role, buildingIds).requireAccessTo(buildingId);
        return alertService.listAlerts(buildingId, status).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Operation(
            summary = "Get an alert",
            description = "Returns one alert with its instructions. When evacuationRequired is true, the residents "
                    + "must evacuate the building (US16)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The alert",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AlertResponseDto.class),
                    examples = {
                            @ExampleObject(name = "Evacuation alert", value = OpenApiExamples.CRITICAL_ALERT_RESPONSE),
                            @ExampleObject(name = "Threshold alert", value = OpenApiExamples.THRESHOLD_ALERT_RESPONSE)
                    }
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "A resident asked for an alert of a building that is not in their token",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Other building",
                            value = OpenApiExamples.RESIDENT_FORBIDDEN_ERROR)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No alert has that identifier",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Unknown alert", value = OpenApiExamples.ALERT_NOT_FOUND_ERROR)
            )
    )
    @GetMapping("/{alertId}")
    public AlertResponseDto getAlert(
            @Parameter(description = "Identifier of the alert", example = "0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a")
            @PathVariable UUID alertId,
            @Parameter(hidden = true) @RequestHeader(value = GatewayUser.ROLE_HEADER, required = false) String role,
            @Parameter(hidden = true) @RequestHeader(value = GatewayUser.BUILDING_IDS_HEADER, required = false)
            String buildingIds
    ) {
        Alert alert = alertService.getAlert(alertId);
        GatewayUser.from(role, buildingIds).requireAccessTo(alert.getBuildingId());
        return mapper.toResponse(alert);
    }

    @Operation(
            summary = "Acknowledge an alert",
            description = "Records that the administrator has seen an OPEN alert. The user comes from the "
                    + "X-User-Id header added by the gateway."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Alert acknowledged",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AlertResponseDto.class),
                    examples = @ExampleObject(name = "Acknowledged alert",
                            value = OpenApiExamples.ACKNOWLEDGED_ALERT_RESPONSE)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No alert has that identifier",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Unknown alert", value = OpenApiExamples.ALERT_NOT_FOUND_ERROR)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "The alert is not OPEN",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Already acknowledged",
                            value = OpenApiExamples.ALERT_ALREADY_ACKNOWLEDGED_ERROR)
            )
    )
    @PatchMapping("/{alertId}/acknowledge")
    public AlertResponseDto acknowledgeAlert(
            @Parameter(description = "Identifier of the alert", example = "0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a")
            @PathVariable UUID alertId,
            @Parameter(hidden = true) @RequestHeader(value = GatewayUser.USER_ID_HEADER, required = false)
            String userId
    ) {
        return mapper.toResponse(alertService.acknowledgeAlert(alertId, GatewayUser.parseUserId(userId)));
    }

    @Operation(
            summary = "Resolve an alert",
            description = "Closes an OPEN or ACKNOWLEDGED alert when no further action is needed. "
                    + "A resolved threshold alert lets the sensor raise a new one."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Alert resolved",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AlertResponseDto.class),
                    examples = @ExampleObject(name = "Resolved alert", value = OpenApiExamples.RESOLVED_ALERT_RESPONSE)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No alert has that identifier",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Unknown alert", value = OpenApiExamples.ALERT_NOT_FOUND_ERROR)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "The alert is already RESOLVED",
            content = @Content(
                    mediaType = OpenApiExamples.PROBLEM_JSON,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(name = "Already resolved",
                            value = OpenApiExamples.ALERT_ALREADY_RESOLVED_ERROR)
            )
    )
    @PatchMapping("/{alertId}/resolve")
    public AlertResponseDto resolveAlert(
            @Parameter(description = "Identifier of the alert", example = "0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a")
            @PathVariable UUID alertId
    ) {
        return mapper.toResponse(alertService.resolveAlert(alertId));
    }
}
