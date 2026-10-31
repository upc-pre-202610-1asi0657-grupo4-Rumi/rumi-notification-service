package com.rumi.notification.domain.model;

import java.util.Locale;

/**
 * Severity of an alert. It uses the same four levels as the risk index of Seismic Correlation,
 * so a risk level maps to a severity by name.
 */
public enum AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /**
     * Converts the risk level published by Seismic Correlation ("LOW", "MEDIUM", "HIGH" or "CRITICAL").
     */
    public static AlertSeverity fromRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            throw new IllegalArgumentException("Risk level is required");
        }
        try {
            return valueOf(riskLevel.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown risk level " + riskLevel + "; expected LOW, MEDIUM, HIGH or CRITICAL");
        }
    }
}
