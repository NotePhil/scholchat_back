package cmr.notep.business.impl;

import cmr.notep.business.business.ProfesseursBusiness;
import cmr.notep.interfaces.api.ProfesseursApi;
import cmr.notep.interfaces.modeles.Professeurs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ProfesseursService implements ProfesseursApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;

    private final ProfesseursBusiness professeursBusiness;

    public ProfesseursService(ProfesseursBusiness professeursBusiness) {
        this.professeursBusiness = professeursBusiness;
    }

    /** Les scans de CNI et le matricule ne sont visibles que du professeur lui-même et des admins. */
    private Professeurs rediger(Professeurs p) {
        if (p != null && !currentUser.isAdmin() && !currentUser.isSelf(p.getId())) {
            p.setCniUrlRecto(null);
            p.setCniUrlVerso(null);
            p.setMatriculeProfesseur(null);
        }
        return p;
    }

    private List<Professeurs> rediger(List<Professeurs> l) {
        if (l != null) l.forEach(this::rediger);
        return l;
    }

    @Override
    public Professeurs avoirProfesseur(@NonNull String idProfesseur) {
        return rediger(professeursBusiness.avoirProfesseur(idProfesseur));
    }

    @Override
    public List<Professeurs> avoirToutProfesseurs() {
        return rediger(professeursBusiness.avoirToutProfesseurs());
    }

    @Override
    public Professeurs posterProfesseur(@NonNull Professeurs professeur) {
        currentUser.requireAdmin();
        return professeursBusiness.posterProfesseur(professeur);
    }

    @Override
    public Professeurs modifierProfesseurPartiellement(@NonNull String idProfesseur, @NonNull Professeurs professeur) {
        currentUser.requireSelfOrAdmin(idProfesseur);
        if (!currentUser.isAdmin()) {
            cmr.notep.interfaces.modeles.Utilisateurs existant = professeursBusiness.avoirProfesseur(idProfesseur);
            if (professeur.getEtat() != null && professeur.getEtat() != existant.getEtat()) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seul un administrateur peut modifier l'état d'un compte.");
            }
            if (professeur.getEmail() != null && !professeur.getEmail().trim().equalsIgnoreCase(String.valueOf(existant.getEmail()))) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("L'adresse email d'un compte ne peut être modifiée que par un administrateur.");
            }
            professeur.setEtat(null);
            professeur.setEmail(null);
        }
        return rediger(professeursBusiness.modifierProfesseurPartiellement(idProfesseur, professeur));
    }

    @Override
    public Professeurs avoirProfesseurParMatricule(@NonNull String matriculeProfesseur) {
        return rediger(professeursBusiness.avoirProfesseurParMatricule(matriculeProfesseur));
    }

    @Override
    public List<Professeurs> avoirProfesseursParClasse(@NonNull String classeId) {
        return rediger(professeursBusiness.avoirProfesseursParClasse(classeId));
    }

    @Override
    public List<Professeurs> avoirCollaborateursProfesseur(@NonNull String moderateurId) {
        return rediger(professeursBusiness.avoirCollaborateursProfesseur(moderateurId));
    }
}