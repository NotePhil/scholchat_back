package cmr.notep.business.impl;

import cmr.notep.business.business.SuiviPedagogiqueBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.AccessControlService;
import cmr.notep.business.security.CurrentUserService;
import cmr.notep.interfaces.api.SuiviPedagogiqueApi;
import cmr.notep.interfaces.dto.ClasseResumeEleveDTO;
import cmr.notep.interfaces.dto.CoursProgrammeResumeDTO;
import cmr.notep.interfaces.dto.ExerciceCoursClasseDTO;
import cmr.notep.interfaces.dto.ProgressionEleveDTO;
import cmr.notep.interfaces.dto.StatistiquesClasseDTO;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Suivi pédagogique : voir {@link SuiviPedagogiqueApi} (contrôles d'accès) et {@link SuiviPedagogiqueBusiness}. */
@RestController
@Slf4j
@RequiredArgsConstructor
public class SuiviPedagogiqueService implements SuiviPedagogiqueApi {

    private final SuiviPedagogiqueBusiness business;
    private final CurrentUserService currentUser;
    private final AccessControlService accessControl;
    private final AccederRepository accederRepository;

    @Override
    public List<CoursProgrammeResumeDTO> resumeCoursProgrammes(String classeId) {
        accessControl.getClasse(classeId);
        accessControl.requireClassMember(classeId);
        return business.resumeCoursProgrammes(classeId);
    }

    @Override
    public List<ExerciceCoursClasseDTO> exercicesDuCours(String classeId, String coursId, String eleveId) {
        accessControl.getClasse(classeId);
        accessControl.requireClassMember(classeId);
        String eleve = eleveId == null || eleveId.isBlank() ? null : eleveId.trim();
        if (eleve != null) {
            requireEleveOuParentOuEnseignant(eleve, classeId);
            if (!accederRepository.existsByUtilisateurIdAndClasseId(eleve, classeId)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Cet élève n'est pas inscrit dans cette classe");
            }
        }
        return business.exercicesDuCours(classeId, coursId, eleve);
    }

    @Override
    public List<ClasseResumeEleveDTO> resumeClassesEleve(String eleveId) {
        accessControl.requireSelfOrParentOrAdmin(eleveId);
        return business.resumeClassesEleve(eleveId);
    }

    @Override
    public ProgressionEleveDTO progressionEleve(String eleveId, String classeId) {
        String classe = classeId == null || classeId.isBlank() ? null : classeId.trim();
        if (classe != null) {
            accessControl.getClasse(classe);
            requireEleveOuParentOuEnseignant(eleveId, classe);
        } else {
            accessControl.requireSelfOrParentOrAdmin(eleveId);
        }
        return business.progressionEleve(eleveId, classe);
    }

    @Override
    public StatistiquesClasseDTO statistiquesClasse(String classeId) {
        accessControl.getClasse(classeId);
        accessControl.requireClassTeacher(classeId);
        return business.statistiquesClasse(classeId);
    }

    /** L'élève lui-même, son parent, un enseignant/gestionnaire de la classe, ou un administrateur. */
    private void requireEleveOuParentOuEnseignant(String eleveId, String classeId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || me.equals(eleveId) || accessControl.isParentOf(me, eleveId)
                || accessControl.canPublishInClass(classeId, me)) {
            return;
        }
        throw CurrentUserService.forbidden("Vous ne pouvez consulter que vos propres résultats (ou ceux de vos enfants).");
    }
}
