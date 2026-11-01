package com.rumi.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumi.notification.infrastructure.seed.DevDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Starts the whole service with the dev profile (H2 and sample data) and runs the alert flow end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class NotificationServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoadsAndTheHealthEndpointAnswers() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("rumi-notification-service"));
    }

    @Test
    void theSampleAlertAndThresholdAreAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/alerts").param("buildingId", DevDataSeeder.BUILDING_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(DevDataSeeder.ALERT_ID.toString())));
        mockMvc.perform(get("/api/v1/thresholds/{sensorId}", DevDataSeeder.SENSOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalValue").value(0.10));
    }

    @Test
    void aSimulatedCriticalRiskRaisesOneEvacuationAlert() throws Exception {
        UUID buildingId = UUID.randomUUID();
        String request = "{\"buildingId\":\"" + buildingId + "\",\"riskIndexId\":\"" + UUID.randomUUID()
                + "\",\"riskLevel\":\"CRITICAL\"}";

        String first = mockMvc.perform(post("/api/v1/alerts/simulations/risk")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evacuationRequired").value(true))
                .andReturn().getResponse().getContentAsString();
        String alertId = objectMapper.readTree(first).get("id").asText();

        mockMvc.perform(post("/api/v1/alerts/simulations/risk")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(alertId));
        mockMvc.perform(get("/api/v1/alerts").param("buildingId", buildingId.toString()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void aSimulatedReadingAboveTheThresholdRaisesOneAlertUntilItIsResolved() throws Exception {
        UUID sensorId = UUID.randomUUID();
        UUID buildingId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/thresholds/{sensorId}", sensorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buildingId\":\"" + buildingId + "\",\"warningValue\":0.05,\"criticalValue\":0.10}"))
                .andExpect(status().isOk());
        String reading = "{\"sensorId\":\"" + sensorId + "\",\"buildingId\":\"" + buildingId
                + "\",\"zone\":\"FLOOR-3-NORTH\",\"value\":0.12}";

        String raised = mockMvc.perform(post("/api/v1/alerts/simulations/readings")
                        .contentType(MediaType.APPLICATION_JSON).content(reading))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/api/v1/alerts/simulations/readings")
                        .contentType(MediaType.APPLICATION_JSON).content(reading))
                .andExpect(status().isNoContent());

        String alertId = objectMapper.readTree(raised).get("id").asText();
        mockMvc.perform(patch("/api/v1/alerts/{alertId}/acknowledge", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
        mockMvc.perform(patch("/api/v1/alerts/{alertId}/resolve", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
        mockMvc.perform(post("/api/v1/alerts/simulations/readings")
                        .contentType(MediaType.APPLICATION_JSON).content(reading))
                .andExpect(status().isCreated());
    }

    @Test
    void theOpenApiDocumentDescribesTheAlertAndThresholdEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/alerts'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/alerts/{alertId}/acknowledge'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/thresholds/{sensorId}'].put").exists());
    }
}
