package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<EvenementEntity, String> {
    List<EvenementEntity> findByCreateurId(String professeurId);
}