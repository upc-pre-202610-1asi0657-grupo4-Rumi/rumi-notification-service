package com.rumi.notification.infrastructure.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the {@link GatewayUser} built from the identity headers of rumi-api-gateway. A request without
 * X-User-Id or X-Role is rejected with 401 before the controller runs.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface CurrentGatewayUser {
}
