package cmr.notep.business.impl;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.CurrentUserService;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.UserRoleEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.UserRoleRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GET /utilisateurs/par-email?email=… — réservé aux administrateurs. Avant de créer un gestionnaire, l'écran
 * vérifie si un compte porte déjà cet e-mail : si oui, l'administrateur confirme qu'il s'agit bien de cette
 * personne (champs en lecture seule) au lieu de créer un compte ou d'ajouter le rôle sans le savoir.
 * Réponse : {existe:false} ou {existe:true, id, prenom, nom, email, telephone, adresse, roles[], dejaGestionnaire}.
 */
@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurRechercheController {

    private final DaoAccessorService daoAccessorService;
    private final CurrentUserService currentUser;

    public UtilisateurRechercheController(DaoAccessorService daoAccessorService, CurrentUserService currentUser) {
        this.daoAccessorService = daoAccessorService;
        this.currentUser = currentUser;
    }

    @GetMapping(path = "/par-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> parEmail(@RequestParam(name = "email", required = false) String email) {
        currentUser.requireAdmin();
        String e = email == null ? "" : email.trim();
        if (e.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'e-mail est requis");
        }
        UtilisateursRepository users = daoAccessorService.getRepository(UtilisateursRepository.class);
        Optional<UtilisateursEntity> trouve = users.findByEmail(e);
        if (trouve.isEmpty()) {
            trouve = users.findByEmail(e.toLowerCase());
        }
        Map<String, Object> vue = new LinkedHashMap<>();
        if (trouve.isEmpty()) {
            vue.put("existe", false);
            return vue;
        }
        UtilisateursEntity u = trouve.get();
        List<String> roles = daoAccessorService.getRepository(UserRoleRepository.class)
                .findByUtilisateurId(u.getId()).stream()
                .map(UserRoleEntity::getRoleType).distinct().toList();
        vue.put("existe", true);
        vue.put("id", u.getId());
        vue.put("prenom", u.getPrenom());
        vue.put("nom", u.getNom());
        vue.put("email", u.getEmail());
        vue.put("telephone", u.getTelephone());
        vue.put("adresse", u.getAdresse());
        vue.put("roles", roles);
        vue.put("dejaGestionnaire", roles.contains("GESTIONNAIRE"));
        return vue;
    }
}
