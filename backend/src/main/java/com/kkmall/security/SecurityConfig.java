package com.kkmall.security;

import com.kkmall.common.interfaces.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) throws Exception {
        http.csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling()
            .authenticationEntryPoint((request, response, ex) -> {
                response.setStatus(401);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), ApiResponse.fail("AUTH_REQUIRED", "AUTH_REQUIRED", null));
            })
            .accessDeniedHandler((request, response, ex) -> {
                response.setStatus(403);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), ApiResponse.fail("AUTH_FORBIDDEN", "AUTH_FORBIDDEN", null));
            })
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/api/v1/auth/**", "/api/v1/admin/auth/login", "/api/v1/categories", "/api/v1/products/**", "/api/v1/files/**").permitAll()
            .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.addExposedHeader("X-Trace-Id");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }
}
