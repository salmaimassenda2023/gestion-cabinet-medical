package com.example.utilisateurservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ OPTIONS requests must be permitted first
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ ENDPOINTS PUBLICS
                        .requestMatchers("/actuator/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/api/utilisateur/users/bootstrap/**").permitAll()
                        .requestMatchers("/api/utilisateur/users/register/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/utilisateur/users/*/cabinet").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/utilisateur/users").permitAll()

                        // ✅ ENDPOINTS PROTÉGÉS
                        .requestMatchers(HttpMethod.DELETE, "/api/utilisateur/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateur/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/utilisateur/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "MEDECIN")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                .decoder(jwtDecoder())));

        return http.build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow specific origins - add your frontend URLs
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:4200",
//                "http://localhost:3000",
//                "http://localhost:8081",
//                "http://localhost:8222" // Also allow gateway
//        ));
//
//        // Allow all HTTP methods
//        configuration.setAllowedMethods(Arrays.asList(
//                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
//        ));
//
//        // Allow all headers
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//
//        // Expose important headers
//        configuration.setExposedHeaders(Arrays.asList(
//                "Authorization",
//                "Content-Type",
//                "Accept",
//                "X-Requested-With"
//        ));
//
//        // Allow credentials
//        configuration.setAllowCredentials(true);
//
//        // Cache preflight for 1 hour
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Configure JWT decoder with your Keycloak server
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:9098/realms/cabinet-medical/protocol/openid-connect/certs").build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            return roles.stream()
                    .filter(role -> !role.startsWith("ROLE_"))
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });

        return jwtAuthenticationConverter;
    }
}