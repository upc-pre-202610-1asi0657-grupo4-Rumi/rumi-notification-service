package com.rumi.notification.infrastructure.web;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User forwarded by rumi-api-gateway in the X-Role and X-Building-Id headers after it validated the JWT.
 * The gateway only checks the role; the rule "a resident only sees the alerts of their own buildings"
 * belongs to this service. A request without the headers did not go through the gateway (for example, a
 * local test) and is not restricted.
 */
record GatewayUser(String role, Set<UUID> buildingIds) {

    static final String ROLE_HEADER = "X-Role";
    static final String BUILDING_IDS_HEADER = "X-Building-Id";
    static final String USER_ID_HEADER = "X-User-Id";
    static final String RESIDENT = "RESIDENT";

    static GatewayUser from(String role, String buildingIdsHeader) {
        if (buildingIdsHeader == null || buildingIdsHeader.isBlank()) {
            return new GatewayUser(role, Set.of());
        }
        try {
            Set<UUID> ids = Arrays.stream(buildingIdsHeader.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toUnmodifiableSet());
            return new GatewayUser(role, ids);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Header " + BUILDING_IDS_HEADER + " must hold building ids");
        }
    }

    static UUID parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Header " + USER_ID_HEADER + " must hold a user id");
        }
    }

    void requireAccessTo(UUID buildingId) {
        if (RESIDENT.equals(role) && !buildingIds.contains(buildingId)) {
            throw new BuildingAccessDeniedException(buildingId);
        }
    }
}
