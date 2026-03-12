package com.kareem.GitMatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Holds the AES encryption key used to encrypt sensitive tokens at rest.
 */
@Configuration
public class EncryptionConfig {

    @Value("${gitmatch.security.encryption-key}")
    private String encryptionKey;

    public String getEncryptionKey() {
        return encryptionKey;
    }
}
