package com.rumi.notification.infrastructure.web;

/**
 * Example payloads shown in the OpenAPI documentation.
 */
final class OpenApiExamples {

    static final String PROBLEM_JSON = "application/problem+json";

    static final String CRITICAL_ALERT_RESPONSE = """
            {
              "id": "6f1d2c3b-4a5e-4f6d-8c7b-9a0e1f2d3c4b",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "type": "SEISMIC_RISK",
              "severity": "CRITICAL",
              "status": "OPEN",
              "title": "Seismic risk CRITICAL",
              "message": "Critical structural risk after the seismic event. Evacuate the building now using the nearest evacuation route.",
              "evacuationRequired": true,
              "riskIndexId": "4d2b8f6e-1a3c-4e5d-9f7b-2c4a6e8d0f13",
              "seismicEventId": "9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b",
              "sensorId": null,
              "zone": null,
              "measuredValue": null,
              "occurredAt": "2026-10-30T14:32:00Z",
              "createdAt": "2026-10-30T14:32:04Z",
              "acknowledgedAt": null,
              "acknowledgedBy": null,
              "resolvedAt": null
            }""";

    static final String THRESHOLD_ALERT_RESPONSE = """
            {
              "id": "2a4c6e8f-0b1d-4f3a-9c5e-7d9f1b3d5f70",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "type": "THRESHOLD_EXCEEDED",
              "severity": "CRITICAL",
              "status": "OPEN",
              "title": "Sensor threshold exceeded",
              "message": "Reading 0.120 in zone FLOOR-3-NORTH reached the critical threshold 0.100 of sensor 5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11.",
              "evacuationRequired": false,
              "riskIndexId": null,
              "seismicEventId": null,
              "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
              "zone": "FLOOR-3-NORTH",
              "measuredValue": 0.12,
              "occurredAt": "2026-10-30T15:10:00Z",
              "createdAt": "2026-10-30T15:10:01Z",
              "acknowledgedAt": null,
              "acknowledgedBy": null,
              "resolvedAt": null
            }""";

    static final String ALERT_LIST_RESPONSE = """
            [
              {
                "id": "0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a",
                "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
                "type": "SEISMIC_RISK",
                "severity": "MEDIUM",
                "status": "OPEN",
                "title": "Seismic risk MEDIUM",
                "message": "Medium structural risk after the seismic event. A structural inspection of the affected zones is recommended.",
                "evacuationRequired": false,
                "riskIndexId": "3c9a1e7d-4b2f-4d8e-a6c1-7e5f3b2d1a90",
                "seismicEventId": "9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b",
                "sensorId": null,
                "zone": null,
                "measuredValue": null,
                "occurredAt": "2026-10-06T14:32:00Z",
                "createdAt": "2026-10-06T14:32:00Z",
                "acknowledgedAt": null,
                "acknowledgedBy": null,
                "resolvedAt": null
              }
            ]""";

    static final String ACKNOWLEDGED_ALERT_RESPONSE = """
            {
              "id": "0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "type": "SEISMIC_RISK",
              "severity": "MEDIUM",
              "status": "ACKNOWLEDGED",
              "title": "Seismic risk MEDIUM",
              "message": "Medium structural risk after the seismic event. A structural inspection of the affected zones is recommended.",
              "evacuationRequired": false,
              "riskIndexId": "3c9a1e7d-4b2f-4d8e-a6c1-7e5f3b2d1a90",
              "seismicEventId": "9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b",
              "sensorId": null,
              "zone": null,
              "measuredValue": null,
              "occurredAt": "2026-10-06T14:32:00Z",
              "createdAt": "2026-10-06T14:32:00Z",
              "acknowledgedAt": "2026-10-30T15:20:00Z",
              "acknowledgedBy": "c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f",
              "resolvedAt": null
            }""";

    static final String RESOLVED_ALERT_RESPONSE = """
            {
              "id": "0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "type": "SEISMIC_RISK",
              "severity": "MEDIUM",
              "status": "RESOLVED",
              "title": "Seismic risk MEDIUM",
              "message": "Medium structural risk after the seismic event. A structural inspection of the affected zones is recommended.",
              "evacuationRequired": false,
              "riskIndexId": "3c9a1e7d-4b2f-4d8e-a6c1-7e5f3b2d1a90",
              "seismicEventId": "9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b",
              "sensorId": null,
              "zone": null,
              "measuredValue": null,
              "occurredAt": "2026-10-06T14:32:00Z",
              "createdAt": "2026-10-06T14:32:00Z",
              "acknowledgedAt": "2026-10-30T15:20:00Z",
              "acknowledgedBy": "c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f",
              "resolvedAt": "2026-10-30T16:05:00Z"
            }""";

    static final String MISSING_BUILDING_ID_ERROR = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Required parameter 'buildingId' is not present.",
              "instance": "/api/v1/alerts"
            }""";

    static final String INVALID_STATUS_ERROR = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Failed to convert 'status' with value: 'CLOSED'",
              "instance": "/api/v1/alerts"
            }""";

    static final String RESIDENT_FORBIDDEN_ERROR = """
            {
              "type": "about:blank",
              "title": "Forbidden",
              "status": 403,
              "detail": "You do not have access to the alerts of building 7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "instance": "/api/v1/alerts"
            }""";

    static final String ALERT_NOT_FOUND_ERROR = """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "Alert 0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a was not found",
              "instance": "/api/v1/alerts/0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a"
            }""";

    static final String ALERT_ALREADY_ACKNOWLEDGED_ERROR = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "Alert 0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a is ACKNOWLEDGED and cannot be acknowledged",
              "instance": "/api/v1/alerts/0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a/acknowledge"
            }""";

    static final String ALERT_ALREADY_RESOLVED_ERROR = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "Alert 0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a is already RESOLVED",
              "instance": "/api/v1/alerts/0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a/resolve"
            }""";

    static final String THRESHOLD_REQUEST = """
            {
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "warningValue": 0.05,
              "criticalValue": 0.10
            }""";

    static final String THRESHOLD_RESPONSE = """
            {
              "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "warningValue": 0.05,
              "criticalValue": 0.10,
              "updatedAt": "2026-10-30T15:00:00Z"
            }""";

    static final String THRESHOLD_LIST_RESPONSE = """
            [
              {
                "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
                "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
                "warningValue": 0.05,
                "criticalValue": 0.10,
                "updatedAt": "2026-10-30T15:00:00Z"
              }
            ]""";

    static final String THRESHOLD_VALIDATION_ERROR = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Invalid request content.",
              "instance": "/api/v1/thresholds/5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
              "errors": {
                "warningValue": "must be greater than 0"
              }
            }""";

    static final String THRESHOLD_LIMITS_ERROR = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Warning value must be lower than the critical value",
              "instance": "/api/v1/thresholds/5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11"
            }""";

    static final String THRESHOLD_NOT_FOUND_ERROR = """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "Sensor 5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11 has no threshold",
              "instance": "/api/v1/thresholds/5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11"
            }""";

    static final String RISK_SIMULATION_REQUEST = """
            {
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "riskLevel": "CRITICAL"
            }""";

    static final String READING_SIMULATION_REQUEST = """
            {
              "sensorId": "5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11",
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "zone": "FLOOR-3-NORTH",
              "value": 0.12
            }""";

    private OpenApiExamples() {
    }
}
