package al.lhind.eventbooking.security;

import al.lhind.eventbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            SecurityErrorResponseWriter errorWriter) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtTokenService, userRepository);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                errorWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                                        "Authentication required"))
                        .accessDeniedHandler((request, response, exception) ->
                                errorWriter.write(request, response, HttpStatus.FORBIDDEN,
                                        "Access denied")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/categories")
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/events/*/waitlist",
                                "/api/v1/events/*/waitlist/**")
                        .hasRole("ATTENDEE")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/events/*/reviews",
                                "/api/v1/events/*/reviews/**")
                        .hasRole("ATTENDEE")

                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/organizer/**")
                        .hasRole("ORGANIZER")

                        .requestMatchers("/api/v1/bookings/**")
                        .hasRole("ATTENDEE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/events",
                                "/api/v1/events/**")
                        .permitAll()

                        .anyRequest()
                        .authenticated())
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}