package com.example.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

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

                                // PUBLIC ENDPOINTS (No Authentication)
                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .pathMatchers("/actuator/**", "/eureka/**").permitAll()

                                // User Registration & Bootstrap
                                .pathMatchers(HttpMethod.POST, "/api/utilisateur/users").permitAll()
                                .pathMatchers("/api/utilisateur/users/bootstrap/**").permitAll()
                                .pathMatchers("/api/utilisateur/users/register/**").permitAll()

                                // Cabinet Management (Public)
                                .pathMatchers(HttpMethod.POST, "/api/cabinet").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/cabinet/*/services").permitAll()
                                .pathMatchers(HttpMethod.GET, "/api/cabinet/**").permitAll()

                                // Payments & Webhooks (Public)
                                .pathMatchers("/api/payments/**").permitAll()
                                .pathMatchers("/api/cabinet/webhooks/**").permitAll()

                                // Rendez-vous Management (Public)
                                .pathMatchers(HttpMethod.GET, "/api/rendezvous/**").permitAll()

                                // Medicament Search (Public)
                                .pathMatchers(HttpMethod.GET, "/api/medicament/**").permitAll()

                                // ADMIN ONLY - User & Cabinet Management
                                .pathMatchers(HttpMethod.DELETE, "/api/utilisateur/users/**")
                                .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")

                                .pathMatchers(HttpMethod.PUT, "/api/utilisateur/users/*/status")
                                .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")

                                .pathMatchers(HttpMethod.DELETE, "/api/cabinet/**")
                                .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")

                                .pathMatchers(HttpMethod.PATCH, "/api/cabinet/*/status")
                                .hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")

                                // MEDECIN ONLY - Medical Records & Consultations

                                // Patient Medical Records (MEDECIN only)
                                .pathMatchers(HttpMethod.GET, "/api/patient/*/dossier")
                                .hasAuthority("ROLE_MEDECIN")

                                .pathMatchers(HttpMethod.PUT, "/api/patient/*/dossier")
                                .hasAuthority("ROLE_MEDECIN")

                                .pathMatchers(HttpMethod.GET, "/api/patient/*/documents")
                                .hasAuthority("ROLE_MEDECIN")

                                .pathMatchers(HttpMethod.POST, "/api/patient/*/documents")
                                .hasAuthority("ROLE_MEDECIN")

                                .pathMatchers(HttpMethod.DELETE, "/api/patient/documents/**")
                                .hasAuthority("ROLE_MEDECIN")

                                // Consultations (MEDECIN only)
                                .pathMatchers("/api/consultation/**")
                                .hasAuthority("ROLE_MEDECIN")

                                // MEDECIN or SECRETAIRE - Patient Basic Info
                                .pathMatchers(HttpMethod.POST, "/api/patient")
                                .hasAnyAuthority("ROLE_MEDECIN", "ROLE_SECRETAIRE")

                                .pathMatchers(HttpMethod.GET, "/api/patient/**")
                                .hasAnyAuthority("ROLE_MEDECIN", "ROLE_SECRETAIRE")

                                .pathMatchers(HttpMethod.PUT, "/api/patient/*")
                                .hasAnyAuthority("ROLE_MEDECIN", "ROLE_SECRETAIRE")

                                .pathMatchers(HttpMethod.DELETE, "/api/patient/*")
                                .hasAnyAuthority("ROLE_MEDECIN", "ROLE_SECRETAIRE")

                                // AUTHENTICATED - General Access
                                .pathMatchers("/api/notification/**").authenticated()

                                // All other requests require authentication
                                .anyExchange().authenticated()
                        )
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        )
                        .build();
        }

        @Bean
        public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
                ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
                converter.setPrincipalClaimName("preferred_username");
                return converter;
        }

        private Flux<GrantedAuthority> extractAuthorities(Jwt jwt) {
                System.out.println("🔐 Gateway - Processing JWT for user: " + jwt.getClaim("preferred_username"));

                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                List<String> roles = new ArrayList<>();

                if (realmAccess != null && realmAccess.containsKey("roles")) {
                        roles = (List<String>) realmAccess.get("roles");
                }

                System.out.println("🔐 Gateway - Extracted roles: " + roles);

                // Convert roles to authorities with ROLE_ prefix
                List<GrantedAuthority> authorities = roles.stream()
                        .filter(role -> !role.startsWith("ROLE_"))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                System.out.println("🔐 Gateway - Authorities: " + authorities);

                return Flux.fromIterable(authorities);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Only allow your frontend
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        @Bean
        public ReactiveJwtDecoder jwtDecoder() {
                return NimbusReactiveJwtDecoder
                        .withJwkSetUri("http://localhost:9098/realms/cabinet-medical/protocol/openid-connect/certs")
                        .build();
        }
}