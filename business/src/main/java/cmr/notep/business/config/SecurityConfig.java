package cmr.notep.business.config;

import cmr.notep.business.security.JwtAuthenticationFilter;
import cmr.notep.business.security.RestAuthenticationEntryPoint;
import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    @Value("${front.endpoint}")
    private String frontEndpoint;

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

                // 401 (jeton absent/invalide/expiré) et 403 (droit insuffisant) en JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAuthenticationEntryPoint)
                )

                // Règles d'autorisation. Tout ce qui n'est pas explicitement public exige un JWT valide ;
                // les contrôles fins (propriétaire, modérateur de classe, auteur…) sont faits dans les
                // contrôleurs via AccessControlService / CurrentUserService.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ── Authentification / activation / mot de passe (avant connexion) ──
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login", "/auth/switch-role", "/auth/activate",
                                "/auth/reset-password-request", "/auth/reset-password",
                                "/auth/registerPassword",          // exige le jeton d'activation (contrôlé dans AuthService)
                                "/auth/users/register",            // exige le jeton d'activation
                                // Vérification du compte par code e-mail (bouton « Vérifier mon compte »)
                                "/auth/verification-compte/envoyer", "/auth/verification-compte/verifier"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/users/byEmail").permitAll() // exige le jeton d'activation
                        // Ancien endpoint qui fixait le mot de passe de n'importe quel email : admin uniquement
                        .requestMatchers("/auth/register").hasRole("ADMIN")

                        // ── Inscription ──
                        .requestMatchers(HttpMethod.POST, "/utilisateurs").permitAll()                       // types publics seulement (contrôlé)
                        .requestMatchers(HttpMethod.POST, "/utilisateurs/regenerate-activation").permitAll()
                        // PATCH anonyme limité aux pièces du professeur en cours d'inscription, avec le jeton de
                        // dépôt (X-Upload-Token) renvoyé par POST /utilisateurs — contrôlé dans UtilisateursService
                        // (AccessControlService#hasSignupUploadAccess). Connecté : soi-même / parent / admin.
                        .requestMatchers(HttpMethod.PATCH, "/utilisateurs/*").permitAll()
                        // Dépôt des pièces pendant l'inscription : anonyme seulement avec le jeton de dépôt et pour
                        // cni-recto / cni-verso / selfie (contrôlé dans MediaServiceImpl) ; sinon utilisateur connecté.
                        .requestMatchers(HttpMethod.POST, "/media/presigned-url", "/media/proxy-upload").permitAll()

                        // ── Liens reçus par email ──
                        // Approbation/rejet de classe par l'établissement : jeton signé du mail OU gestionnaire connecté
                        .requestMatchers(HttpMethod.POST,
                                "/etablissements/approve-class/*/*", "/etablissements/reject-class/*/*").permitAll()
                        // Renouvellement d'offre par jeton
                        .requestMatchers(HttpMethod.POST, "/contrats/renouvellement-info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/contrats/renouvellement/*").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/contrats/renouvellement/*/prolonger", "/contrats/renouvellement/*/changer-offre").permitAll()
                        // Catalogue des offres (non sensible, affiché sur la page de renouvellement)
                        .requestMatchers(HttpMethod.GET, "/offres", "/offres/*").permitAll()

                        // ── Divers publics ──
                        .requestMatchers(HttpMethod.GET, "/public/jitsi-branding").permitAll()
                        // Aperçu d'une classe par son code avant inscription (limité par IP, voir ClasseApercuController)
                        .requestMatchers(HttpMethod.GET, "/public/classes/apercu").permitAll()
                        // Poignée de main WebSocket/SockJS : l'authentification se fait sur la trame STOMP CONNECT
                        // (WebSocketAuthInterceptor), pas sur la requête HTTP d'upgrade.
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        // ── Réservé aux administrateurs ──
                        .requestMatchers("/admin/**", "/test/**", "/actuator/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/utilisateurs/professors/pending", "/utilisateurs/professors/pending/**",
                                "/utilisateurs/professors/*/validate",
                                "/utilisateurs/professeurs/*/rejet",
                                "/utilisateurs/validerProfesseur/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/utilisateurs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/motifsRejets", "/motifsRejetClasses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/motifsRejets/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/motifsRejets/*", "/motifsRejetClasses/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/matieres").hasAnyRole("ADMIN", "PROFESSOR")
                        .requestMatchers(HttpMethod.PUT, "/matieres/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/matieres/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/etablissements").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/etablissements/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/professeurs", "/parents", "/repetiteurs").hasRole("ADMIN")
                        // Un parent crée le profil de son enfant (AddChildModal) ; champs sensibles neutralisés dans ElevesService
                        .requestMatchers(HttpMethod.POST, "/profil-eleves").hasAnyRole("ADMIN", "PARENT")
                        // NB : GET /profil-eleves et /parents/summary restent ouverts aux connectés mais sont
                        // filtrés (utilisateurs liés uniquement) pour les non-admins, voir ElevesService/ParentsService.
                        .requestMatchers(HttpMethod.GET,
                                "/parents", "/repetiteurs", "/gestionnaires",
                                "/canaux", "/cours-programmes", "/histo-activations/actives",
                                "/histo-activations/etat/*", "/classes/by-status").hasRole("ADMIN")
                        .requestMatchers("/histo-activations", "/histo-activations/*/desactivation").hasRole("ADMIN")

                        // ── Sessions de cours en direct ──
                        .requestMatchers(HttpMethod.POST,
                                "/cours/*/session/start",
                                "/cours/*/session/*/end",
                                "/cours/*/session/*/chapter"
                        ).hasAnyRole("PROFESSOR", "ADMIN")

                        // ── Tout le reste : utilisateur authentifié ──
                        .anyRequest().authenticated()
                )

                // Use stateless session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authentication provider
                .authenticationProvider(daoAuthenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontEndpoint, "https://scholchat-front-1.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "X-Timezone",
                "X-Upload-Token"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        // The self-hosted Jitsi web app fetches /public/jitsi-branding directly
        // from the Jitsi domain (not ours), so it can never carry credentials
        // and must stay openly readable — it's non-sensitive (just a logo URL
        // and a color). Registered as its own pattern since it's more specific
        // than "/**" above and Spring picks the best-matching registration.
        CorsConfiguration openBranding = new CorsConfiguration();
        openBranding.setAllowedOrigins(List.of("*"));
        openBranding.setAllowedMethods(List.of("GET", "OPTIONS"));
        openBranding.setAllowedHeaders(List.of("*"));
        openBranding.setAllowCredentials(false);
        openBranding.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/public/jitsi-branding", openBranding);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private DaoAuthenticationProvider daoAuthenticationProvider() {
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