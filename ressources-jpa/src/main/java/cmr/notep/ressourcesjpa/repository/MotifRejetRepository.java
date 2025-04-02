package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotifRejetRepository extends JpaRepository<MotifRejetEntity, String> {
    Optional<MotifRejetEntity> findByCode(String code);
}
