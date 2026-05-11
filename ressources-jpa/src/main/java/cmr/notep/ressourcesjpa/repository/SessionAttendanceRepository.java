package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.SessionAttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionAttendanceRepository extends JpaRepository<SessionAttendanceEntity, String> {
    
    List<SessionAttendanceEntity> findBySessionId(String sessionId);
    
    List<SessionAttendanceEntity> findByCoursId(String coursId);
    
    Optional<SessionAttendanceEntity> findBySessionIdAndUserId(String sessionId, String userId);
    
    List<SessionAttendanceEntity> findByUserIdAndCoursId(String userId, String coursId);
    
    @Query("SELECT sa FROM SessionAttendanceEntity sa WHERE sa.sessionId = :sessionId AND sa.status = :status")
    List<SessionAttendanceEntity> findBySessionIdAndStatus(@Param("sessionId") String sessionId, 
                                                          @Param("status") SessionAttendanceEntity.AttendanceStatus status);
    
    @Query("SELECT COUNT(sa) FROM SessionAttendanceEntity sa WHERE sa.sessionId = :sessionId AND sa.status = 'JOINED'")
    long countActiveParticipants(@Param("sessionId") String sessionId);
}