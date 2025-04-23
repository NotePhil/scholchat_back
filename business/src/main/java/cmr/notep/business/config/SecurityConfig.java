package cmr.notep.business.config;

import cmr.notep.business.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for API and H2 console
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure headers for H2 console using the new API
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - add both with and without trailing slash
                        .requestMatchers(
                                "/auth/register", "/auth/register/",
                                "/auth/login", "/auth/login/",
                                "/auth/activate", "/auth/activate/",
                                "/auth/refresh", "/auth/refresh/",
                                "/utilisateurs", "/utilisateurs/",
                                "/auth/users/byEmail",
                                "/auth/users/register",
                                "/reset-password-request"
                        ).permitAll()
                        .requestMatchers(
                                "/utilisateurs/professeurs/*/rejet",
                                "/utilisateurs/validerProfesseur/**",
                                "/utilisateurs/professors/pending/**",
                                "/motifsRejets/**"  // Added endpoint for motifs rejets

                        ).hasRole("ADMIN")
                        // Enable debug logging for matchers
                        .requestMatchers("/actuator/**").permitAll()

                        // H2 Console endpoints
                        .requestMatchers("/h2-console/**", "/scholchat/h2-console/**",
                                "/h2/**", "/scholchat/h2/**").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Professor validation endpoints - admin only
                        .requestMatchers("/utilisateurs/validerProfesseur/**").hasRole("ADMIN")
                        .requestMatchers("/utilisateurs/professors/pending/**").hasRole("ADMIN")

                        // Secure all other endpoints
                        .anyRequest().authenticated()
                )

                // Use stateless session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}