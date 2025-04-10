package cmr.notep.business.security;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.awt.font.TextHitInfo;
import java.util.ArrayList;
import java.util.List;

@Service

@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateursBusiness utilisateursBusiness;

    public CustomUserDetailsService(UtilisateursBusiness utilisateursBusiness) {
        this.utilisateursBusiness = utilisateursBusiness;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        try {
            Utilisateurs utilisateur = utilisateursBusiness.avoirUtilisateurParEmail(email);

            if (utilisateur == null) {
                log.warn("User not found with email: {}", email);
                throw new UsernameNotFoundException("User not found with email: " + email);
            }

            // Check if account is active
            boolean enabled = utilisateur.getEtat() == EtatUtilisateur.ACTIVE;

            // Extract authorities (roles)
            List<SimpleGrantedAuthority> authorities = getAuthorities(utilisateur);

            log.debug("User loaded successfully: {} with roles: {}", email, authorities);

            return new User(
                    utilisateur.getEmail(),
                    utilisateur.getPasseAccess(),
                    enabled,
                    true,  // account non-expired
                    true,  // credentials non-expired
                    true,  // account non-locked
                    authorities
            );

        } catch (Exception e) {
            log.error("Error loading user: {}", email, e);
            throw new UsernameNotFoundException("Error loading user: " + email, e);
        }
    }

    /**
     * Determine authorities based on user type and admin status.
     */
    private List<SimpleGrantedAuthority> getAuthorities(Utilisateurs utilisateur) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Add base ROLE_USER
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Admin role
        if (utilisateur.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // Add roles based on user type
        if (utilisateur instanceof Professeurs) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PROFESSOR"));

            // Add extra role if professor is validated
            if (utilisateur.getEtat() == EtatUtilisateur.VALIDATED) {
                authorities.add(new SimpleGrantedAuthority("ROLE_VALIDATED_PROFESSOR"));
            }
        } else if (utilisateur instanceof Eleves) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        } else if (utilisateur instanceof Parents) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PARENT"));
        } else if (utilisateur instanceof Repetiteurs) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TUTOR"));
        }

        return authorities;
    }
}
