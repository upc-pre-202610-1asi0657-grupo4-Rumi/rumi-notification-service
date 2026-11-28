package com.rumi.notification.infrastructure.web;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User forwarded by rumi-api-gateway in the X-User-Id, X-Role and X-Building-Id headers after it validated the
 * JWT. The gateway only checks the role; building membership is checked here, for every role:
 * <ul>
 *     <li>a request without X-User-Id or X-Role did not go through the gateway and is rejected (401);</li>
 *     <li>only ADMINISTRATOR and RESIDENT reach buildings, and only the buildings of their token (403);</li>
 *     <li>actions that change an alert or a threshold are for the ADMINISTRATOR of the building (403).</li>
 * </ul>
 * Platform roles do not exist yet; when they arrive they get their own rule, never access by default.
 */
record GatewayUser(UUID userId, String role, Set<UUID> buildingIds) {

    static final String ROLE_HEADER = "X-Role";
    static final String BUILDING_IDS_HEADER = "X-Building-Id";
    static final String USER_ID_HEADER = "X-User-Id";
    static final String ADMINISTRATOR = "ADMINISTRATOR";
    static final String RESIDENT = "RESIDENT";

    static GatewayUser from(String userIdHeader, String roleHeader, String buildingIdsHeader) {
        if (isBlank(userIdHeader) || isBlank(roleHeader)) {
            throw new MissingGatewayIdentityException();
        }
        return new GatewayUser(parseUserId(userIdHeader), roleHeader.trim(), parseBuildingIds(buildingIdsHeader));
    }

    /**
     * The user may read the data of the building: an ADMINISTRATOR or a RESIDENT of that building.
     */
    void requireAccessTo(UUID buildingId) {
        boolean buildingRole = ADMINISTRATOR.equals(role) || RESIDENT.equals(role);
        if (!buildingRole || !buildingIds.contains(buildingId)) {
            throw BuildingAccessDeniedException.forBuilding(buildingId);
        }
    }

    /**
     * The user may change the alerts and thresholds of the building: only its ADMINISTRATOR.
     */
    void requireAdministratorOf(UUID buildingId) {
        requireAccessTo(buildingId);
        if (!ADMINISTRATOR.equals(role)) {
            throw BuildingAccessDeniedException.administratorOnly();
        }
    }

    private static UUID parseUserId(String userIdHeader) {
        try {
            return UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Header " + USER_ID_HEADER + " must hold a user id");
        }
    }

    private static Set<UUID> parseBuildingIds(String buildingIdsHeader) {
        if (isBlank(buildingIdsHeader)) {
            return Set.of();
        }
        try {
            return Arrays.stream(buildingIdsHeader.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Header " + BUILDING_IDS_HEADER + " must hold building ids");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
