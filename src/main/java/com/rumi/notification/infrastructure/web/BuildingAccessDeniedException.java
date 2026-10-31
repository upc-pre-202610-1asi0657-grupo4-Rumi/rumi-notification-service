package com.rumi.notification.infrastructure.web;

import java.util.UUID;

/**
 * A resident asked for the alerts of a building that is not in their token.
 */
public class BuildingAccessDeniedException extends RuntimeException {

    public BuildingAccessDeniedException(UUID buildingId) {
        super("You do not have access to the alerts of building " + buildingId);
    }
}
