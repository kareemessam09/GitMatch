package com.kareem.GitMatch.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


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
