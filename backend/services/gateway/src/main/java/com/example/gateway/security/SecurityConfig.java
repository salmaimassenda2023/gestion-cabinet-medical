package com.example.gateway.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration de sécurité pour le Gateway Service (WebFlux - Réactif)
 * Cette configuration s'applique à TOUTES les routes qui passent par le gateway
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // ✅ ENDPOINTS PUBLICS - Pas d'authentification requise
                        .pathMatchers(
                                "/api/utilisateur/users/bootstrap/**",
                                "/api/utilisateur/users/register/**",
                                "/api/cabinet/webhooks/stripe/**",
                                "/actuator/**",
                                "/eureka/**"
                        ).permitAll()

                        // ✅ TOUS LES AUTRES ENDPOINTS - Authentification requise
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );

        return http.build();
    }
}