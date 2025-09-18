package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, String> {
    List<MediaEntity> findByOwnerId(String ownerId);

    Optional<MediaEntity> findByFilePath(String filePath);

    List<MediaEntity> findByMediaType(String mediaType);

    Optional<MediaEntity> findByFileNameAndOwnerId(String fileName, String ownerId);

    // Updated method to handle potential duplicates
    List<MediaEntity> findByFileName(String fileName);

    // New method to get unique result by file name with owner ID
    @Query("SELECT m FROM MediaEntity m WHERE m.fileName = :fileName AND m.ownerId IS NOT NULL ORDER BY m.uploadedDate DESC")
    Optional<MediaEntity> findLatestByFileName(@Param("fileName") String fileName);

    // New method to get unique result by file path with additional validation
    @Query("SELECT m FROM MediaEntity m WHERE m.filePath = :filePath AND m.ownerId IS NOT NULL")
    Optional<MediaEntity> findByFilePathWithOwner(@Param("filePath") String filePath);
}