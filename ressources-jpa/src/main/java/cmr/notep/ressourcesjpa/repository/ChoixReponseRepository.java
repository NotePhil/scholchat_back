package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ChoixReponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoixReponseRepository extends JpaRepository<ChoixReponseEntity, String> {
}
