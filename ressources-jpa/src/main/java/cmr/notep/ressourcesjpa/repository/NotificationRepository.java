package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
    
    Long countByUserIdAndIsReadFalse(String userId);
    
    void deleteByUserId(String userId);
}
