package com.example.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                        .csrf(ServerHttpSecurity.CsrfSpec::disable)
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .authorizeExchange(exchange -> exchange
                                // ============================================
                                // PUBLIC ENDPOINTS - MUST BE FIRST
                                // ============================================
                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .pathMatchers("/actuator/**", "/eureka/**").permitAll()

                                // User Registration & Bootstrap
                                .pathMatchers(HttpMethod.POST, "/api/utilisateur/users").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/utilisateur/users/").permitAll()
                                .pathMatchers("/api/utilisateur/users/bootstrap/**").permitAll()
                                .pathMatchers("/api/utilisateur/users/register/**").permitAll()
                                .pathMatchers(HttpMethod.PATCH, "/api/utilisateur/users/*/cabinet").permitAll()

                                // Cabinet Creation & Management (public for registration)
                                .pathMatchers(HttpMethod.POST, "/api/cabinet", "/api/cabinet/").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/cabinet/*/services", "/api/cabinet/*/services/").permitAll()
                                .pathMatchers(HttpMethod.GET, "/api/cabinet/**").permitAll()

                                // Payment Processing (Stripe)
                                .pathMatchers("/api/payments/**").permitAll()
                                .pathMatchers("/api/cabinet/webhooks/stripe/**").permitAll()

                                // ============================================
                                // PROTECTED ENDPOINTS - Require Authentication
                                // ============================================
                                .anyExchange().authenticated())
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())))
                        .build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Allow specific origins
                configuration.setAllowedOrigins(Arrays.asList(
                        "http://localhost:4200",
                        "http://localhost:3000",
                        "http://localhost:8081"
                ));

                // Allow all HTTP methods
                configuration.setAllowedMethods(Arrays.asList(
                        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
                ));

                // Allow all headers
                configuration.setAllowedHeaders(Arrays.asList("*"));

                // Expose headers that the frontend needs to read
                configuration.setExposedHeaders(Arrays.asList(
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Credentials",
                        "Authorization",
                        "Content-Type"
                ));

                // Allow credentials (cookies, authorization headers)
                configuration.setAllowCredentials(true);

                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        @Bean
        public ReactiveJwtDecoder jwtDecoder() {
                // Configure JWT decoder with your Keycloak server
                return NimbusReactiveJwtDecoder
                        .withJwkSetUri("http://localhost:9098/realms/cabinet-medical/protocol/openid-connect/certs")
                        .build();
        }
}