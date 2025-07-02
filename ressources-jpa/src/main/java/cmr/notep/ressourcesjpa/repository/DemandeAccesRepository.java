package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatDemandeAcces;
import cmr.notep.ressourcesjpa.dao.DemandeAccesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeAccesRepository extends JpaRepository<DemandeAccesEntity, String> {
    List<DemandeAccesEntity> findByUtilisateurId(String utilisateurId);
    List<DemandeAccesEntity> findByClasseId(String classeId);
    Optional<DemandeAccesEntity> findByUtilisateurIdAndClasseId(String utilisateurId, String classeId);
    List<DemandeAccesEntity> findByEtat(EtatDemandeAcces etat);

    // J'ajoute ces méthodes car elles n'existaient pas
    List<DemandeAccesEntity> findByClasseIdAndEtat(String classeId, EtatDemandeAcces etat);
    boolean existsByUtilisateurIdAndClasseId(String utilisateurId, String classeId);
}