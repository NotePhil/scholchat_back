package cmr.notep.business.security;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // List of public endpoints that should bypass authentication
    private final List<String> publicEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/activate",
            "/auth/refresh",
            "/h2-console",
            "/scholchat/h2-console",
            "/h2",
            "/scholchat/h2",
            "/scholchat/utilisateurs"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        log.debug("Checking if path should be filtered: {}", path);

        boolean shouldSkip = publicEndpoints.stream().anyMatch(path::startsWith);

        if (shouldSkip) {
            log.debug("Skipping JWT authentication for public endpoint: {}", path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.debug("Processing request: {}", request.getServletPath());

        // Check if Authorization header exists and has the correct format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid authorization header found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from Authorization header
        jwt = authHeader.substring(7);
        log.debug("JWT token extracted from header");

        try {
            // Extract user email from token
            userEmail = jwtUtil.getEmailFromToken(jwt);
            log.debug("Email extracted from token: {}", userEmail);

            // Extract roles from token
            List<String> roles = jwtUtil.getRolesFromToken(jwt);
            log.debug("Roles extracted from token: {}", roles);

            // If email exists and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("User details loaded for: {}", userEmail);

                // Validate token
                if (jwtUtil.validateToken(jwt)) {
                    // Convert roles to SimpleGrantedAuthority
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );

                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Update security context with authentication
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User authenticated: {}", userEmail);
                }
            }
        } catch (SchoolException e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
    }
}
