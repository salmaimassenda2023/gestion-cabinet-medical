package com.example.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange
                        // ✅ CRITIQUE : Autoriser OPTIONS sans JWT
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Endpoint de bootstrap PUBLIC (sans authentification)
                        .pathMatchers("/api/utilisateur/users/bootstrap/**").permitAll()

                        // ✅ Endpoints publics pour Swagger/Actuator (si nécessaire)
                        .pathMatchers("/actuator/**", "/api-docs/**", "/swagger-ui/**").permitAll()

                        // ✅ Tous les autres endpoints nécessitent une authentification
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().authenticated()
                )
                // ✅ Validation des JWT de Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}