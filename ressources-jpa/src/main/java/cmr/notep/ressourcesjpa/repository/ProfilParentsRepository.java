package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ProfilParentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilParentsRepository extends JpaRepository<ProfilParentsEntity, String> {
    // You can add custom query methods here if needed
}