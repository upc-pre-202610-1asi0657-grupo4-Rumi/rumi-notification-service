package com.rumi.notification.infrastructure.web;

import com.rumi.notification.AlertFixtures;
import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.application.AlertNotFoundException;
import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.AlertStateException;
import com.rumi.notification.domain.model.AlertStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
@Import(AlertDtoMapper.class)
class AlertControllerTest {

    private static final UUID BUILDING_ID = AlertFixtures.BUILDING_ID;
    private static final UUID OTHER_BUILDING_ID = UUID.fromString("1f2e3d4c-5b6a-4978-8695-a4b3c2d1e0f9");
    private static final UUID USER_ID = UUID.fromString("c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertApplicationService alertService;

    @Test
    void listsTheAlertsOfABuilding() throws Exception {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.CRITICAL);
        when(alertService.listAlerts(BUILDING_ID, null)).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/v1/alerts").param("buildingId", BUILDING_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(alert.getId().toString()))
                .andExpect(jsonPath("$[0].type").value("SEISMIC_RISK"))
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].evacuationRequired").value(true))
                .andExpect(jsonPath("$[0].createdAt").value("2026-10-30T14:32:04Z"));
    }

    @Test
    void filtersTheAlertsByStatus() throws Exception {
        when(alertService.listAlerts(BUILDING_ID, AlertStatus.OPEN)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/alerts")
                        .param("buildingId", BUILDING_ID.toString())
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(alertService).listAlerts(BUILDING_ID, AlertStatus.OPEN);
    }

    @Test
    void rejectsAListWithoutBuildingId() throws Exception {
        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(alertService);
    }

    @Test
    void rejectsAnUnknownStatus() throws Exception {
        mockMvc.perform(get("/api/v1/alerts")
                        .param("buildingId", BUILDING_ID.toString())
                        .param("status", "CLOSED"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(alertService);
    }

    @Test
    void aResidentCanListTheAlertsOfTheirOwnBuilding() throws Exception {
        when(alertService.listAlerts(BUILDING_ID, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/alerts")
                        .param("buildingId", BUILDING_ID.toString())
                        .header("X-Role", "RESIDENT")
                        .header("X-Building-Id", OTHER_BUILDING_ID + ", " + BUILDING_ID))
                .andExpect(status().isOk());
    }

    @Test
    void aResidentCannotListTheAlertsOfAnotherBuilding() throws Exception {
        mockMvc.perform(get("/api/v1/alerts")
                        .param("buildingId", BUILDING_ID.toString())
                        .header("X-Role", "RESIDENT")
                        .header("X-Building-Id", OTHER_BUILDING_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail")
                        .value("You do not have access to the alerts of building " + BUILDING_ID));

        verifyNoInteractions(alertService);
    }

    @Test
    void anAdministratorCanListTheAlertsOfAnyBuilding() throws Exception {
        when(alertService.listAlerts(BUILDING_ID, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/alerts")
                        .param("buildingId", BUILDING_ID.toString())
                        .header("X-Role", "ADMINISTRATOR")
                        .header("X-Building-Id", OTHER_BUILDING_ID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsAThresholdAlertWithItsSensorReading() throws Exception {
        Alert alert = AlertFixtures.thresholdAlert();
        when(alertService.getAlert(alert.getId())).thenReturn(alert);

        mockMvc.perform(get("/api/v1/alerts/{alertId}", alert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("THRESHOLD_EXCEEDED"))
                .andExpect(jsonPath("$.sensorId").value(AlertFixtures.SENSOR_ID.toString()))
                .andExpect(jsonPath("$.zone").value("FLOOR-3-NORTH"))
                .andExpect(jsonPath("$.measuredValue").value(0.12))
                .andExpect(jsonPath("$.riskIndexId").doesNotExist());
    }

    @Test
    void aResidentCannotGetAnAlertOfAnotherBuilding() throws Exception {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.HIGH);
        when(alertService.getAlert(alert.getId())).thenReturn(alert);

        mockMvc.perform(get("/api/v1/alerts/{alertId}", alert.getId())
                        .header("X-Role", "RESIDENT")
                        .header("X-Building-Id", OTHER_BUILDING_ID.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void answersNotFoundForAnUnknownAlert() throws Exception {
        UUID alertId = UUID.randomUUID();
        when(alertService.getAlert(alertId)).thenThrow(new AlertNotFoundException(alertId));

        mockMvc.perform(get("/api/v1/alerts/{alertId}", alertId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Alert " + alertId + " was not found"));
    }

    @Test
    void acknowledgesAnAlertOnBehalfOfTheGatewayUser() throws Exception {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.MEDIUM);
        alert.acknowledge(USER_ID, AlertFixtures.NOW);
        when(alertService.acknowledgeAlert(alert.getId(), USER_ID)).thenReturn(alert);

        mockMvc.perform(patch("/api/v1/alerts/{alertId}/acknowledge", alert.getId())
                        .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledgedBy").value(USER_ID.toString()));
    }

    @Test
    void rejectsAMalformedUserIdHeader() throws Exception {
        mockMvc.perform(patch("/api/v1/alerts/{alertId}/acknowledge", UUID.randomUUID())
                        .header("X-User-Id", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Header X-User-Id must hold a user id"));

        verifyNoInteractions(alertService);
    }

    @Test
    void answersConflictWhenTheAlertIsNotOpen() throws Exception {
        UUID alertId = UUID.randomUUID();
        when(alertService.acknowledgeAlert(alertId, null)).thenThrow(
                new AlertStateException("Alert " + alertId + " is ACKNOWLEDGED and cannot be acknowledged"));

        mockMvc.perform(patch("/api/v1/alerts/{alertId}/acknowledge", alertId))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void resolvesAnAlert() throws Exception {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.LOW);
        alert.resolve(AlertFixtures.NOW);
        when(alertService.resolveAlert(alert.getId())).thenReturn(alert);

        mockMvc.perform(patch("/api/v1/alerts/{alertId}/resolve", alert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").value("2026-10-30T14:32:04Z"));
    }
}
