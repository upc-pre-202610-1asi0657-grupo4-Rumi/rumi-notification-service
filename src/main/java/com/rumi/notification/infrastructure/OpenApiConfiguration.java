package com.rumi.notification.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Rumi Notification Service API")
                .description("REST API of the Alerts & Notification bounded context: alerts raised by seismic "
                        + "risk (US14, US16) or by sensor readings that exceed a threshold (US15), and the "
                        + "thresholds of each sensor.")
                .version("0.2.0"));
    }
}
