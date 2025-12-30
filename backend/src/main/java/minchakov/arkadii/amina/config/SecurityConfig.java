package minchakov.arkadii.amina.config;

import minchakov.arkadii.amina.filter.JWTFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public SecurityConfig(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JWTFilter jWTFilter, ObjectMapper objectMapper) {
        return http.authorizeHttpRequests(auth -> auth.requestMatchers(
                       "/api/v1/auth/login",
                       "/api/v1/auth/register",
                       "/ws/**",
                       "/ws"
                   ).permitAll().anyRequest().authenticated())
                   .csrf(AbstractHttpConfigurer::disable)
                   .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                   .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                   .addFilterAfter(jWTFilter, LogoutFilter.class)
                   .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> handlerExceptionResolver.resolveException(
                       request,
                       response,
                       null,
                       authException
                   )))
                   .build();
    }

    @Bean
    public FilterRegistrationBean<JWTFilter> jwtFilterRegistration(JWTFilter filter) {
        FilterRegistrationBean<JWTFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
