package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessagesRepository extends JpaRepository<MessagesEntity, String> {
}
