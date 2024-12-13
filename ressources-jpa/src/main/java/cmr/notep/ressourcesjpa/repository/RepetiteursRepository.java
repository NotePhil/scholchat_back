package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.RepetiteursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepetiteursRepository extends JpaRepository<RepetiteursEntity, String> {
}
