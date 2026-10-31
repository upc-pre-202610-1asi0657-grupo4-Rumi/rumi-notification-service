package com.rumi.notification.domain.model;

/**
 * Life cycle of an alert: OPEN when raised, ACKNOWLEDGED when an administrator has seen it,
 * RESOLVED when no further action is needed.
 */
public enum AlertStatus {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED
}
