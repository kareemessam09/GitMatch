package com.kareem.GitMatch.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Injects the encryption key into the static JPA AttributeConverter at startup.
 * This bridges Spring's DI with JPA's converter lifecycle.
 */
@Component
public class TokenEncryptionInitializer {

    private final EncryptionConfig encryptionConfig;

    public TokenEncryptionInitializer(EncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    @PostConstruct
    public void init() {
        TokenEncryptionConverter.setEncryptionKey(encryptionConfig.getEncryptionKey());
    }
}
