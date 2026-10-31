package com.rumi.notification.infrastructure.seed;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.SensorThreshold;
import com.rumi.notification.domain.repository.AlertRepository;
import com.rumi.notification.domain.repository.SensorThresholdRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Loads one threshold and one alert of the sample building when the service runs with the dev profile.
 * The building and sensor ids are the fixed ids of the sample data of rumi-building-service.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    public static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    public static final UUID SENSOR_ID = UUID.fromString("5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11");
    public static final UUID ALERT_ID = UUID.fromString("0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a");
    public static final UUID RISK_INDEX_ID = UUID.fromString("3c9a1e7d-4b2f-4d8e-a6c1-7e5f3b2d1a90");
    public static final UUID SEISMIC_EVENT_ID = UUID.fromString("9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b");

    private static final Instant SAMPLE_TIME = Instant.parse("2026-10-06T14:32:00Z");

    private final AlertRepository alertRepository;
    private final SensorThresholdRepository thresholdRepository;

    public DevDataSeeder(AlertRepository alertRepository, SensorThresholdRepository thresholdRepository) {
        this.alertRepository = alertRepository;
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        thresholdRepository.save(new SensorThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10, SAMPLE_TIME));
        alertRepository.save(Alert.fromSeismicRisk(
                ALERT_ID, BUILDING_ID, RISK_INDEX_ID, SEISMIC_EVENT_ID,
                AlertSeverity.MEDIUM, SAMPLE_TIME, SAMPLE_TIME));
    }
}
