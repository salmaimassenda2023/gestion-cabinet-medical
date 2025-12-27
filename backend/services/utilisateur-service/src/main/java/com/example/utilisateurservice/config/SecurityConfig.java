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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;
import java.util.stream.Collectors;

///**
// * Configuration de sécurité pour le Service Utilisateur (Web MVC -
// * Traditionnel)
// * Notez l'utilisation de HttpSecurity (pas ServerHttpSecurity)
// */
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // ✅ ENDPOINTS PUBLICS - DOIVENT ÊTRE EN PREMIER
//                        .requestMatchers("/actuator/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
//                        .permitAll()
//                        .requestMatchers("/api/utilisateur/users/bootstrap/**").permitAll()
//                        .requestMatchers("/api/utilisateur/users/register/**").permitAll()
//                        .requestMatchers(HttpMethod.PATCH, "/api/utilisateur/users/*/cabinet").permitAll() // Internal
//                                                                                                           // sync
//
//                        // ✅ ENDPOINTS PROTÉGÉS
//                        .requestMatchers(HttpMethod.POST, "/api/utilisateur/users")
//                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "MEDECIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/utilisateur/users/**")
//                        .hasAnyRole("SUPER_ADMIN", "ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/api/utilisateur/users/me").authenticated()
//                        .requestMatchers(HttpMethod.GET, "/api/utilisateur/users/**")
//                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "MEDECIN")
//
//                        .anyRequest().authenticated())
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
//
//        return http.build();
//    }
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//
//            if (realmAccess == null || !realmAccess.containsKey("roles")) {
//                return Collections.emptyList();
//            }
//
//            @SuppressWarnings("unchecked")
//            List<String> roles = (List<String>) realmAccess.get("roles");
//
//            return roles.stream()
//                    .filter(role -> !role.startsWith("ROLE_"))
//                    .map(role -> "ROLE_" + role)
//                    .map(SimpleGrantedAuthority::new)
//                    .collect(Collectors.toList());
//        });
//
//        return jwtAuthenticationConverter;
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8222"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Use default CORS configuration
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

                        // ✅ ENDPOINTS PROTÉGÉS
                        .requestMatchers(HttpMethod.POST, "/api/utilisateur/users")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "MEDECIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/utilisateur/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateur/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/utilisateur/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "MEDECIN")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
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

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8222"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
//        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}