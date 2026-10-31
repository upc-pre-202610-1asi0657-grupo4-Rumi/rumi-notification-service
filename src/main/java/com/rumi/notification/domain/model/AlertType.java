package com.rumi.notification.domain.model;

/**
 * What raised an alert.
 */
public enum AlertType {
    /** Seismic Correlation calculated the risk index of a building after a seismic event (US14, US16). */
    SEISMIC_RISK,
    /** A sensor reading exceeded the threshold configured for that sensor (US15). */
    THRESHOLD_EXCEEDED
}
