package com.rumi.notification.infrastructure.web;

import java.util.UUID;

/**
 * The user of the gateway may not reach the building, or may reach it but the action is for its administrator.
 */
public class BuildingAccessDeniedException extends RuntimeException {

    private BuildingAccessDeniedException(String message) {
        super(message);
    }

    static BuildingAccessDeniedException forBuilding(UUID buildingId) {
        return new BuildingAccessDeniedException("You do not have access to building " + buildingId);
    }

    static BuildingAccessDeniedException administratorOnly() {
        return new BuildingAccessDeniedException("Only the administrator of the building can do this");
    }
}
