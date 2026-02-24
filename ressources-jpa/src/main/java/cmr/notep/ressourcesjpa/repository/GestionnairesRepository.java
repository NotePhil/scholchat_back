package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.GestionnairesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GestionnairesRepository extends JpaRepository<GestionnairesEntity, String> {
}
