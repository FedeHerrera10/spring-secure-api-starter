package com.fedeherrera.spring_secure_api_starter.security.config;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fedeherrera.spring_secure_api_starter.filter.JwtAuthFilter;
import com.fedeherrera.spring_secure_api_starter.filter.RateLimitFilter;
import com.fedeherrera.spring_secure_api_starter.security.exception.CustomAccessDeniedHandler;
import com.fedeherrera.spring_secure_api_starter.security.exception.JwtAuthenticationEntryPoint;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    private final JwtAuthFilter jwtAuthFilter; // filtro JWT
    private final JwtAuthenticationEntryPoint unauthorizedHandler; // Inyectamos nuestro nuevo componente
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final RateLimitFilter rateLimitFilter;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] AUTH_WHITELIST = {
            "/auth/register",
            "/auth/login",
            "/auth/verify",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/oauth2/**",
            "/auth/google/login",
            "/auth/refresh-token"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. CORS & CSRF
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // 2. Sesiones Stateless (Sin estado)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Manejo de Errores (401 y 403)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler))

                // 4. Autorización por grupos de rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_WHITELIST).permitAll() // Documentación
                        .requestMatchers(AUTH_WHITELIST).permitAll() // Auth endpoints
                        .anyRequest().authenticated() // Todo lo demás bloqueado
                )

                // 5. OAuth2 Login (Si usas el flujo de redirección de Spring)
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/auth/oauth2/success", true))

                // 6 . Filtro de rating limit 
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                // 7. El Filtro JWT debe ir antes del de usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Convertimos el String separado por comas en una lista
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
