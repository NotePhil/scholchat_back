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

    List<MediaEntity> findByCoursId(String coursId);

    // Several rows can share a path or name (re-uploads): the single-result lookups
    // below return the most recent one instead of failing on a non-unique result.
    Optional<MediaEntity> findFirstByFilePathOrderByUploadedDateDesc(String filePath);

    default Optional<MediaEntity> findByFilePath(String filePath) {
        return findFirstByFilePathOrderByUploadedDateDesc(filePath);
    }

    List<MediaEntity> findByMediaType(String mediaType);

    Optional<MediaEntity> findFirstByFileNameAndOwnerIdOrderByUploadedDateDesc(String fileName, String ownerId);

    default Optional<MediaEntity> findByFileNameAndOwnerId(String fileName, String ownerId) {
        return findFirstByFileNameAndOwnerIdOrderByUploadedDateDesc(fileName, ownerId);
    }

    @Query("SELECT m FROM MediaEntity m WHERE m.ownerId = :ownerId AND m.fileName LIKE %:fileNamePart% ORDER BY m.uploadedDate DESC")
    List<MediaEntity> findByOwnerIdAndFileNameContaining(@Param("ownerId") String ownerId, @Param("fileNamePart") String fileNamePart);

    // Updated method to handle potential duplicates
    List<MediaEntity> findByFileName(String fileName);

    // New method to get unique result by file name with owner ID
    Optional<MediaEntity> findFirstByFileNameAndOwnerIdIsNotNullOrderByUploadedDateDesc(String fileName);

    default Optional<MediaEntity> findLatestByFileName(String fileName) {
        return findFirstByFileNameAndOwnerIdIsNotNullOrderByUploadedDateDesc(fileName);
    }

    // New method to get unique result by file path with additional validation
    Optional<MediaEntity> findFirstByFilePathAndOwnerIdIsNotNullOrderByUploadedDateDesc(String filePath);

    default Optional<MediaEntity> findByFilePathWithOwner(String filePath) {
        return findFirstByFilePathAndOwnerIdIsNotNullOrderByUploadedDateDesc(filePath);
    }
}