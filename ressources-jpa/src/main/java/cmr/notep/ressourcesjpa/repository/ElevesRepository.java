package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevesRepository extends JpaRepository<ElevesEntity, String> {
}
