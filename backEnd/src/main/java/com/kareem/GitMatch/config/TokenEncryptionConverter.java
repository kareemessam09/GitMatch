package com.kareem.GitMatch.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA AttributeConverter that encrypts/decrypts sensitive strings (e.g., GitHub tokens)
 * using AES-256-GCM before storing them in PostgreSQL.
 *
 * The encryption key is injected via a static holder set at application startup.
 */
@Converter
public class TokenEncryptionConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(TokenEncryptionConverter.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Static key holder — set by {@link TokenEncryptionInitializer} at startup.
     */
    private static volatile String encryptionKey;

    static void setEncryptionKey(String key) {
        encryptionKey = key;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(attribute.getBytes());

            // Prepend IV to ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt token", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] decoded = Base64.getDecoder().decode(dbData);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(ciphertext);

            return new String(decrypted);
        } catch (Exception e) {
            log.error("Failed to decrypt token", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
