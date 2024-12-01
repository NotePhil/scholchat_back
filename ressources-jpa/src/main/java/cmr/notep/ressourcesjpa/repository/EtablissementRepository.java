package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtablissementRepository extends JpaRepository<EtablissementEntity, String> {
}
