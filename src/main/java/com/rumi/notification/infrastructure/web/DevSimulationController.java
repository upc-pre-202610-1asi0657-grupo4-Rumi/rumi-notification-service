package com.rumi.notification.infrastructure.web;

import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.application.CreateAlertFromRiskCommand;
import com.rumi.notification.application.EvaluateReadingCommand;
import com.rumi.notification.infrastructure.web.dto.AlertResponseDto;
import com.rumi.notification.infrastructure.web.dto.ReadingSimulationRequestDto;
import com.rumi.notification.infrastructure.web.dto.RiskSimulationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

/**
 * Development-only endpoints that run the two use cases the messaging listeners will call, so the alert
 * flow can be shown with Postman without RabbitMQ. They exist only with the dev profile and are not
 * counted as functional endpoints.
 */
@RestController
@Profile("dev")
@RequestMapping("/api/v1/alerts/simulations")
@Tag(name = "Simulations (dev only)", description = "Raise alerts by hand while the messaging listeners are not running")
public class DevSimulationController {

    private final AlertApplicationService alertService;
    private final AlertDtoMapper mapper;
    private final Clock clock;

    public DevSimulationController(AlertApplicationService alertService, AlertDtoMapper mapper, Clock clock) {
        this.alertService = alertService;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Operation(
            summary = "Simulate a risk index of Seismic Correlation",
            description = "Calls createAlertFromRisk, the method the RabbitMQ listener of the risk event calls. "
                    + "Sending the same riskIndexId twice returns the same alert."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RiskSimulationRequestDto.class),
                    examples = @ExampleObject(name = "Critical risk", value = OpenApiExamples.RISK_SIMULATION_REQUEST)
            )
    )
    @ApiResponse(
            responseCode = "201",
            description = "Alert raised (or the alert already raised for that risk index)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AlertResponseDto.class),
                    examples = @ExampleObject(name = "Evacuation alert", value = OpenApiExamples.CRITICAL_ALERT_RESPONSE)
            )
    )
    @PostMapping("/risk")
    public ResponseEntity<AlertResponseDto> simulateRisk(@Valid @RequestBody RiskSimulationRequestDto request) {
        CreateAlertFromRiskCommand command = new CreateAlertFromRiskCommand(
                request.buildingId(),
                Optional.ofNullable(request.riskIndexId()).orElseGet(UUID::randomUUID),
                Optional.ofNullable(request.seismicEventId()).orElseGet(UUID::randomUUID),
                request.riskLevel(),
                clock.instant()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(alertService.createAlertFromRisk(command)));
    }

    @Operation(
            summary = "Simulate a sensor reading",
            description = "Compares the reading with the threshold of the sensor (US15). Answers 201 with the alert "
                    + "when the threshold is exceeded, and 204 when no alert is raised."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ReadingSimulationRequestDto.class),
                    examples = @ExampleObject(name = "Reading above critical",
                            value = OpenApiExamples.READING_SIMULATION_REQUEST)
            )
    )
    @ApiResponse(
            responseCode = "201",
            description = "Threshold exceeded: alert raised",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AlertResponseDto.class),
                    examples = @ExampleObject(name = "Threshold alert", value = OpenApiExamples.THRESHOLD_ALERT_RESPONSE)
            )
    )
    @ApiResponse(responseCode = "204", description = "No alert: no threshold, reading below it, or an active alert exists")
    @PostMapping("/readings")
    public ResponseEntity<AlertResponseDto> simulateReading(@Valid @RequestBody ReadingSimulationRequestDto request) {
        EvaluateReadingCommand command = new EvaluateReadingCommand(
                request.sensorId(), request.buildingId(), request.zone(), request.value(), clock.instant());
        return alertService.evaluateReading(command)
                .map(alert -> ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(alert)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
