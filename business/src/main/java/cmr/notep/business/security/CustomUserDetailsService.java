package cmr.notep.business.security;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.business.services.RoleService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateursBusiness utilisateursBusiness;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Utilisateurs utilisateur = utilisateursBusiness.avoirUtilisateurParEmail(email);

            boolean enabled = utilisateur.getEtat() == EtatUtilisateur.ACTIVE;
            List<SimpleGrantedAuthority> authorities = roleService.determineUserRoles(utilisateur)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return new User(
                    utilisateur.getEmail(),
                    utilisateur.getPasseAccess(),
                    enabled,
                    true, true, true,
                    authorities
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error loading user: " + email, e);
        }
    }

    /**
     * Determine authorities based on user type and admin status. utiliser la méthode de determination des roles
     */
    private List<SimpleGrantedAuthority> getAuthorities(Utilisateurs utilisateur) {
        return roleService.determineUserRoles(utilisateur).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
