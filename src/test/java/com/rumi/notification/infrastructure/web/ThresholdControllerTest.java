package com.rumi.notification.infrastructure.web;

import com.rumi.notification.AlertFixtures;
import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.application.ThresholdNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThresholdController.class)
@Import(ThresholdDtoMapper.class)
class ThresholdControllerTest {

    private static final UUID BUILDING_ID = AlertFixtures.BUILDING_ID;
    private static final UUID SENSOR_ID = AlertFixtures.SENSOR_ID;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertApplicationService alertService;

    private static String body(String buildingId, String warning, String critical) {
        return "{\"buildingId\":" + buildingId + ",\"warningValue\":" + warning
                + ",\"criticalValue\":" + critical + "}";
    }

    @Test
    void configuresTheThresholdOfASensor() throws Exception {
        when(alertService.configureThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10))
                .thenReturn(AlertFixtures.threshold());

        mockMvc.perform(put("/api/v1/thresholds/{sensorId}", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + BUILDING_ID + "\"", "0.05", "0.10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value(SENSOR_ID.toString()))
                .andExpect(jsonPath("$.buildingId").value(BUILDING_ID.toString()))
                .andExpect(jsonPath("$.warningValue").value(0.05))
                .andExpect(jsonPath("$.criticalValue").value(0.10));
    }

    @Test
    void rejectsANegativeWarningValue() throws Exception {
        mockMvc.perform(put("/api/v1/thresholds/{sensorId}", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + BUILDING_ID + "\"", "-0.05", "0.10")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.warningValue").value("must be greater than 0"));

        verifyNoInteractions(alertService);
    }

    @Test
    void rejectsAThresholdWithoutBuilding() throws Exception {
        mockMvc.perform(put("/api/v1/thresholds/{sensorId}", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("null", "0.05", "0.10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.buildingId").value("must not be null"));

        verifyNoInteractions(alertService);
    }

    @Test
    void rejectsAWarningValueAboveTheCriticalValue() throws Exception {
        when(alertService.configureThreshold(SENSOR_ID, BUILDING_ID, 0.20, 0.10)).thenThrow(
                new IllegalArgumentException("Warning value must be lower than the critical value"));

        mockMvc.perform(put("/api/v1/thresholds/{sensorId}", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + BUILDING_ID + "\"", "0.20", "0.10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Warning value must be lower than the critical value"));
    }

    @Test
    void returnsTheThresholdOfASensor() throws Exception {
        when(alertService.getThreshold(SENSOR_ID)).thenReturn(AlertFixtures.threshold());

        mockMvc.perform(get("/api/v1/thresholds/{sensorId}", SENSOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").value("2026-10-30T14:32:04Z"));
    }

    @Test
    void answersNotFoundForASensorWithoutThreshold() throws Exception {
        when(alertService.getThreshold(SENSOR_ID)).thenThrow(new ThresholdNotFoundException(SENSOR_ID));

        mockMvc.perform(get("/api/v1/thresholds/{sensorId}", SENSOR_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Sensor " + SENSOR_ID + " has no threshold"));
    }

    @Test
    void listsTheThresholdsOfABuilding() throws Exception {
        when(alertService.listThresholds(BUILDING_ID)).thenReturn(List.of(AlertFixtures.threshold()));

        mockMvc.perform(get("/api/v1/thresholds").param("buildingId", BUILDING_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value(SENSOR_ID.toString()));
    }
}
