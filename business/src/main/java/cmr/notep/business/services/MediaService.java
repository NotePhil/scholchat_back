package cmr.notep.business.services;

public interface MediaService {
    String generateUploadPresignedUrl(String filePath, String contentType);
    String generateDownloadPresignedUrl(String filePath);
    void evictDownloadUrlCache(String filePath);
    void deleteMedia(String filePath);
    boolean doesObjectExist(String filePath);
    String getDefaultBucketName();
    void moveMedia(String sourcePath, String destinationPath);
    void ensureFolderExists(String folderPath);
}