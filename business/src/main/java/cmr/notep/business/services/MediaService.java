package cmr.notep.business.services;

public interface MediaService {
    String generateUploadPresignedUrl(String filePath, String contentType);
    String generateDownloadPresignedUrl(String filePath);
    void deleteMedia(String filePath);
    boolean doesObjectExist(String filePath);
    String getDefaultBucketName();
}