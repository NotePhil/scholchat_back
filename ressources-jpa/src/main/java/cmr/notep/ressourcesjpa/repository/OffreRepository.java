package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.TypeCibleOffre;
import cmr.notep.ressourcesjpa.dao.OffreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<OffreEntity, String> {
    List<OffreEntity> findByCibleAndActifTrue(TypeCibleOffre cible);
    List<OffreEntity> findByCible(TypeCibleOffre cible);
}
