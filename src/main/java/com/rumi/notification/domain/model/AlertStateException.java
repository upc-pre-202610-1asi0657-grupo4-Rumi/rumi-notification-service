package com.rumi.notification.domain.model;

/**
 * An alert was asked to move to a status that its life cycle does not allow.
 */
public class AlertStateException extends RuntimeException {

    public AlertStateException(String message) {
        super(message);
    }
}
