Feature: Alerts backed by PostgreSQL
  The Notification service raises alerts from the risk indexes of Seismic Correlation and from sensor
  readings that exceed a threshold, stores them in PostgreSQL and lets the administrator handle them.

  Background:
    Given a building of the Rumi network

  Scenario: A risk index raises an alert for the building (US14)
    When Seismic Correlation reports a "HIGH" risk index for the building
    Then the building has 1 "OPEN" alert with severity "HIGH"
    And the alert does not require evacuation

  Scenario: A critical risk index asks the residents to evacuate (US16)
    When Seismic Correlation reports a "CRITICAL" risk index for the building
    Then the building has 1 "OPEN" alert with severity "CRITICAL"
    And the alert requires evacuation

  Scenario: A risk index delivered twice raises only one alert
    When Seismic Correlation reports a "MEDIUM" risk index for the building
    And the same risk index is delivered again
    Then the building has 1 "OPEN" alert with severity "MEDIUM"

  Scenario: A reading above the critical value of its sensor raises a threshold alert (US15)
    Given the administrator sets the threshold of a sensor to warning 0.05 and critical 0.10
    When the sensor records 0.12 in zone "FLOOR-3-NORTH"
    Then the building has 1 "OPEN" alert with severity "CRITICAL"
    And the alert is about the sensor in zone "FLOOR-3-NORTH"

  Scenario: A reading below the warning value raises no alert
    Given the administrator sets the threshold of a sensor to warning 0.05 and critical 0.10
    When the sensor records 0.01 in zone "FLOOR-3-NORTH"
    Then the building has no alerts

  Scenario: The administrator acknowledges and then resolves an alert
    Given Seismic Correlation reports a "HIGH" risk index for the building
    When the administrator acknowledges the alert
    Then the alert status is "ACKNOWLEDGED"
    When the administrator resolves the alert
    Then the alert status is "RESOLVED"
    And acknowledging the alert again is rejected with status 409

  Scenario: A resident cannot read the alerts of another building
    Given Seismic Correlation reports a "LOW" risk index for the building
    When a resident of another building lists the alerts of the building
    Then the request is rejected with status 403
