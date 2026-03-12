package com.devbookmark.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ ADMIN - only ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ✅ AUTH - public
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // ✅ DOCS & MONITORING
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        // Only health check is public — metrics/env/etc require auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").authenticated()

                        // ✅ SEARCH - public
                        .requestMatchers("/api/search/**").permitAll()

                        // ✅ EXPLORE - public
                        .requestMatchers(HttpMethod.GET, "/api/explore/**").permitAll()

                        // ✅ RESOURCES - public GETs
                        .requestMatchers(HttpMethod.GET, "/api/resources/preview").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resources/**").permitAll()

                        // ✅ COLLECTIONS - public GET
                        .requestMatchers(HttpMethod.GET, "/api/collections/*").permitAll()

                        // ✅ TAGS/SERIES/PINS - public GET
                        .requestMatchers(HttpMethod.GET, "/api/tags/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/series/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pins/**").permitAll()

                        // ✅ ACTIVITIES - public
                        .requestMatchers(HttpMethod.GET, "/api/activities/**").permitAll()

                        // ✅ PUBLICATIONS - public GET
                        .requestMatchers(HttpMethod.GET, "/api/publications/**").permitAll()

                        // ✅ View tracking - allow anonymous views
                        .requestMatchers(HttpMethod.POST, "/api/resources/*/view").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resources/*/stats").permitAll()

                        // ✅ ME endpoints (MUST be above /api/users/*)
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me/password").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()

                        // ✅ USERS - public profiles
                        .requestMatchers(HttpMethod.GET, "/api/users/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/followers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/following").permitAll()

                        // ✅ PROTECTED - auth required
                        .requestMatchers("/api/recommendations/**").authenticated()
                        .requestMatchers("/api/suggestions/**").authenticated()
                        .requestMatchers("/api/resources/batch/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()
                        // ✅ everything else requires auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}