package com.rumi.notification.application;

import java.util.UUID;

public class AlertNotFoundException extends ResourceNotFoundException {

    public AlertNotFoundException(UUID alertId) {
        super("Alert " + alertId + " was not found");
    }
}
