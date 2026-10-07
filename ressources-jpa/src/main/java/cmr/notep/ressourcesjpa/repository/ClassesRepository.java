package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatClasse;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<ClassesEntity, String> {
    List<ClassesEntity> findByEtat(EtatClasse etat);

    @Query("SELECT c FROM ClassesEntity c WHERE c.codeActivation = :token AND c.etat = :etat")
    List<ClassesEntity> findByActivationTokenAndEtat(String token, EtatClasse etat);

    default List<ClassesEntity> findByActivationToken(String token) {
        return findByActivationTokenAndEtat(token, EtatClasse.ACTIF);
    }

    boolean existsByCodeActivationAndEtat(String token, EtatClasse etat);

    /** Toutes les classes portant ce code d'activation (quel que soit leur état). */
    List<ClassesEntity> findByCodeActivation(String codeActivation);

    List<ClassesEntity> findByEtablissementIdAndEtat(String etablissementId, EtatClasse etat);

    long countByEtablissementIdAndEtat(String etablissementId, EtatClasse etat);

    /** Classes dont l'utilisateur est le modérateur principal (sans passer par ProfesseursEntity). */
    List<ClassesEntity> findByModeratorId(String moderatorId);

}
