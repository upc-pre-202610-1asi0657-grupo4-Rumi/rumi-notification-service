package com.rumi.notification.infrastructure.web;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves the parameters annotated with {@link CurrentGatewayUser}.
 */
class GatewayUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentGatewayUser.class)
                && GatewayUser.class.equals(parameter.getParameterType());
    }

    @Override
    public GatewayUser resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return GatewayUser.from(
                webRequest.getHeader(GatewayUser.USER_ID_HEADER),
                webRequest.getHeader(GatewayUser.ROLE_HEADER),
                webRequest.getHeader(GatewayUser.BUILDING_IDS_HEADER));
    }
}
