package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, String> {
    List<MediaEntity> findByOwnerId(String ownerId);
    Optional<MediaEntity> findByFilePath(String filePath);
    List<MediaEntity> findByMediaType(String mediaType);
    Optional<MediaEntity> findByFileNameAndOwnerId(String fileName, String ownerId);
}