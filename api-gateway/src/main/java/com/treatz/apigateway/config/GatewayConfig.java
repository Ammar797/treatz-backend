// api-gateway/src/main/java/com/treatz/apigateway/config/GatewayConfig.java
package com.treatz.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - Port 9001
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://AUTH-SERVICE"))

                // Restaurant Service - Port 9002
                .route("restaurant-service", r -> r.path("/api/restaurants/**", "/api/menu-items/**")
                        .uri("lb://RESTAURANT-SERVICE"))

                // Order Service - Port 9003
                .route("order-service", r -> r.path("/api/orders/**")
                        .uri("lb://ORDER-SERVICE"))

                // Notification Service - Port 9004 (if needed for health checks)
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("lb://NOTIFICATION-SERVICE"))

                // Dispatch Service - Port 9005 (if needed for admin endpoints)
                .route("dispatch-service", r -> r.path("/api/dispatch/**")
                        .uri("lb://DISPATCH-SERVICE"))

                .build();
    }
}