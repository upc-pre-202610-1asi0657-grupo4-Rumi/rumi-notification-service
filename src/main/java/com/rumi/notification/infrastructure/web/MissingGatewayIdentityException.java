package com.rumi.notification.infrastructure.web;

/**
 * The request has no X-User-Id or X-Role header: it did not go through rumi-api-gateway.
 */
public class MissingGatewayIdentityException extends RuntimeException {

    public MissingGatewayIdentityException() {
        super("The request has no user identity from the API gateway");
    }
}
