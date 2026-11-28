package com.rumi.notification.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.application.CreateAlertFromRiskCommand;
import com.rumi.notification.application.EvaluateReadingCommand;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Risk indexes and sensor readings are handed to AlertApplicationService directly, exactly as the
 * RabbitMQ listeners do; everything the administrator and the residents do goes through the REST API.
 */
public class AlertStepDefinitions {

    private final NotificationIntegrationTestConfiguration context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AlertApplicationService alertService;

    private UUID buildingId;
    private UUID sensorId;
    private CreateAlertFromRiskCommand lastRisk;
    private UUID alertId;
    private ResponseEntity<String> response;

    public AlertStepDefinitions(NotificationIntegrationTestConfiguration context) {
        this.context = context;
    }

    @Before
    public void resetScenarioState() {
        buildingId = null;
        sensorId = null;
        lastRisk = null;
        alertId = null;
        response = null;
    }

    @Given("a building of the Rumi network")
    public void aBuilding() {
        buildingId = UUID.randomUUID();
    }

    @When("Seismic Correlation reports a {string} risk index for the building")
    public void reportRisk(String riskLevel) {
        lastRisk = new CreateAlertFromRiskCommand(
                buildingId, UUID.randomUUID(), UUID.randomUUID(), riskLevel, Instant.now());
        alertId = alertService.createAlertFromRisk(lastRisk).getId();
    }

    @When("the same risk index is delivered again")
    public void deliverRiskAgain() {
        alertService.createAlertFromRisk(lastRisk);
    }

    @Given("the administrator sets the threshold of a sensor to warning {double} and critical {double}")
    public void configureThreshold(double warning, double critical) {
        sensorId = UUID.randomUUID();
        response = send(HttpMethod.PUT, "/api/v1/thresholds/" + sensorId,
                "{\"buildingId\":\"" + buildingId + "\",\"warningValue\":" + warning
                        + ",\"criticalValue\":" + critical + "}", administratorOf(buildingId));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @When("the sensor records {double} in zone {string}")
    public void recordReading(double value, String zone) {
        alertService.evaluateReading(new EvaluateReadingCommand(sensorId, buildingId, zone, value, Instant.now()))
                .ifPresent(alert -> alertId = alert.getId());
    }

    @When("the administrator acknowledges the alert")
    public void acknowledge() {
        response = send(HttpMethod.PATCH, "/api/v1/alerts/" + alertId + "/acknowledge", null,
                administratorOf(buildingId));
    }

    @When("the administrator resolves the alert")
    public void resolve() {
        response = send(HttpMethod.PATCH, "/api/v1/alerts/" + alertId + "/resolve", null,
                administratorOf(buildingId));
    }

    @When("a resident of the building acknowledges the alert")
    public void residentAcknowledges() {
        response = send(HttpMethod.PATCH, "/api/v1/alerts/" + alertId + "/acknowledge", null,
                gatewayUser("RESIDENT", buildingId));
    }

    @When("a resident of another building lists the alerts of the building")
    public void residentOfAnotherBuildingLists() {
        response = send(HttpMethod.GET, "/api/v1/alerts?buildingId=" + buildingId, null,
                gatewayUser("RESIDENT", UUID.randomUUID()));
    }

    @When("an administrator of another building lists the alerts of the building")
    public void administratorOfAnotherBuildingLists() {
        response = send(HttpMethod.GET, "/api/v1/alerts?buildingId=" + buildingId, null,
                administratorOf(UUID.randomUUID()));
    }

    @Then("the building has {int} {string} alert with severity {string}")
    public void buildingHasAlerts(int count, String status, String severity) throws Exception {
        JsonNode alerts = listAlerts();
        assertThat(alerts).hasSize(count);
        alerts.forEach(alert -> {
            assertThat(alert.get("status").asText()).isEqualTo(status);
            assertThat(alert.get("severity").asText()).isEqualTo(severity);
        });
    }

    @Then("the building has no alerts")
    public void buildingHasNoAlerts() throws Exception {
        assertThat(listAlerts()).isEmpty();
    }

    @Then("the alert requires evacuation")
    public void alertRequiresEvacuation() throws Exception {
        JsonNode alert = getAlert();
        assertThat(alert.get("evacuationRequired").asBoolean()).isTrue();
        assertThat(alert.get("message").asText()).contains("Evacuate the building");
    }

    @Then("the alert does not require evacuation")
    public void alertDoesNotRequireEvacuation() throws Exception {
        assertThat(getAlert().get("evacuationRequired").asBoolean()).isFalse();
    }

    @Then("the alert is about the sensor in zone {string}")
    public void alertIsAboutTheSensor(String zone) throws Exception {
        JsonNode alert = getAlert();
        assertThat(alert.get("type").asText()).isEqualTo("THRESHOLD_EXCEEDED");
        assertThat(alert.get("sensorId").asText()).isEqualTo(sensorId.toString());
        assertThat(alert.get("zone").asText()).isEqualTo(zone);
    }

    @Then("the alert status is {string}")
    public void alertStatusIs(String status) throws Exception {
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(objectMapper.readTree(response.getBody()).get("status").asText()).isEqualTo(status);
    }

    @Then("acknowledging the alert again is rejected with status {int}")
    public void acknowledgeAgainIsRejected(int status) {
        acknowledge();
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the request is rejected with status {int}")
    public void requestIsRejected(int status) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }

    private JsonNode listAlerts() throws Exception {
        response = send(HttpMethod.GET, "/api/v1/alerts?buildingId=" + buildingId, null, administratorOf(buildingId));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody());
    }

    private JsonNode getAlert() throws Exception {
        response = send(HttpMethod.GET, "/api/v1/alerts/" + alertId, null, administratorOf(buildingId));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody());
    }

    /**
     * Identity headers that rumi-api-gateway adds after it validates the JWT.
     */
    private static HttpHeaders gatewayUser(String role, UUID buildingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", UUID.randomUUID().toString());
        headers.set("X-Role", role);
        headers.set("X-Building-Id", buildingId.toString());
        return headers;
    }

    private static HttpHeaders administratorOf(UUID buildingId) {
        return gatewayUser("ADMINISTRATOR", buildingId);
    }

    private ResponseEntity<String> send(HttpMethod method, String path, String body, HttpHeaders headers) {
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return context.http.exchange(context.url(path), method, new HttpEntity<>(body, headers), String.class);
    }
}
