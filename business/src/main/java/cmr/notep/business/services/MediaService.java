package cmr.notep.business.services;

/**
 * Service interface for handling media storage operations
 */
public interface MediaService {

    /**
     * Generate a presigned URL for uploading a file
     *
     * @param filePath    The path to store the file
     * @param contentType The content type of the file
     * @return The presigned URL for upload
     */
    String generateUploadPresignedUrl(String filePath, String contentType);

    /**
     * Generate a presigned URL for downloading a file
     *
     * @param filePath The path of the file in storage
     * @return The presigned URL for download
     */
    String generateDownloadPresignedUrl(String filePath);

    /**
     * Delete a file from storage
     *
     * @param filePath The path of the file to delete
     */
    void deleteMedia(String filePath);

    /**
     * Check if a file exists in storage
     *
     * @param filePath The path of the file to check
     * @return true if the file exists, false otherwise
     */
    boolean doesObjectExist(String filePath);

    /**
     * Get the default bucket name
     *
     * @return The default bucket name
     */
    String getDefaultBucketName();
}