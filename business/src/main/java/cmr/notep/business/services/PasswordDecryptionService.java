package cmr.notep.business.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Service
@Slf4j
public class PasswordDecryptionService {
    
    @Value("${app.encryption.key:scholchat-secure-key-2024-v1-32b}")
    private String secretKey;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SALTED_PREFIX = "Salted__";
    
    public String decryptPassword(String encryptedPassword) {
        try {
            // Decode base64
            byte[] encrypted = Base64.getDecoder().decode(encryptedPassword);
            
            // Check if it's CryptoJS format (starts with "Salted__")
            if (encrypted.length >= 16 && 
                Arrays.equals(Arrays.copyOfRange(encrypted, 0, 8), SALTED_PREFIX.getBytes())) {
                
                // Extract salt (8 bytes after "Salted__")
                byte[] salt = Arrays.copyOfRange(encrypted, 8, 16);
                byte[] ciphertext = Arrays.copyOfRange(encrypted, 16, encrypted.length);
                
                // Derive key and IV using EVP_BytesToKey equivalent
                byte[][] keyIv = deriveKeyAndIV(secretKey.getBytes(StandardCharsets.UTF_8), salt, 32, 16);
                byte[] key = keyIv[0];
                byte[] iv = keyIv[1];
                
                // Decrypt
                SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
                
                byte[] decryptedBytes = cipher.doFinal(ciphertext);
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } else {
                // Fallback to simple AES decryption
                byte[] keyBytes = Arrays.copyOf(secretKey.getBytes(StandardCharsets.UTF_8), 32);
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                
                byte[] decryptedBytes = cipher.doFinal(encrypted);
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Password decryption failed", e);
            throw new RuntimeException("Password decryption failed", e);
        }
    }
    
    /**
     * Equivalent to OpenSSL's EVP_BytesToKey function
     */
    private byte[][] deriveKeyAndIV(byte[] password, byte[] salt, int keyLength, int ivLength) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] derivedBytes = new byte[keyLength + ivLength];
        int derivedLength = 0;
        byte[] hash = null;
        
        while (derivedLength < keyLength + ivLength) {
            md5.reset();
            if (hash != null) {
                md5.update(hash);
            }
            md5.update(password);
            md5.update(salt);
            hash = md5.digest();
            
            int copyLength = Math.min(hash.length, derivedBytes.length - derivedLength);
            System.arraycopy(hash, 0, derivedBytes, derivedLength, copyLength);
            derivedLength += copyLength;
        }
        
        byte[] key = Arrays.copyOfRange(derivedBytes, 0, keyLength);
        byte[] iv = Arrays.copyOfRange(derivedBytes, keyLength, keyLength + ivLength);
        
        return new byte[][]{key, iv};
    }
}