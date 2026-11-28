package com.rumi.notification.infrastructure.web;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adds the identity headers that rumi-api-gateway sets after it validates the JWT.
 */
final class GatewayRequests {

    static final UUID USER_ID = UUID.fromString("c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f");

    private GatewayRequests() {
    }

    static RequestPostProcessor administratorOf(UUID... buildingIds) {
        return user("ADMINISTRATOR", buildingIds);
    }

    static RequestPostProcessor residentOf(UUID... buildingIds) {
        return user("RESIDENT", buildingIds);
    }

    static RequestPostProcessor user(String role, UUID... buildingIds) {
        return request -> {
            request.addHeader("X-User-Id", USER_ID.toString());
            request.addHeader("X-Role", role);
            request.addHeader("X-Building-Id",
                    Arrays.stream(buildingIds).map(UUID::toString).collect(Collectors.joining(",")));
            return request;
        };
    }
}
