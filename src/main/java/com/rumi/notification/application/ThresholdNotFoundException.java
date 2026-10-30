package com.rumi.notification.application;

import java.util.UUID;

public class ThresholdNotFoundException extends ResourceNotFoundException {

    public ThresholdNotFoundException(UUID sensorId) {
        super("Sensor " + sensorId + " has no threshold");
    }
}
