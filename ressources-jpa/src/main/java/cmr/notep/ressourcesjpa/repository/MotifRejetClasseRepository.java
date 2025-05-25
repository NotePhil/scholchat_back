package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MotifRejetClasseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotifRejetClasseRepository extends JpaRepository<MotifRejetClasseEntity, String> {
    Optional<MotifRejetClasseEntity> findByCode(String code);
}