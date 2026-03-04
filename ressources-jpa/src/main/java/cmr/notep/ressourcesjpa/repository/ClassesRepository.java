package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.DroitPublication;
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

    List<ClassesEntity> findByDroitPublicationAndEtat(DroitPublication droitPublication, EtatClasse etat);

}