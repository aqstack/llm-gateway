package com.llmgateway.service;

import com.llmgateway.model.ApiKey;
import com.llmgateway.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public record ApiKeyCreationResult(String rawKey, ApiKey apiKey) {}

    public ApiKeyCreationResult createApiKey(String name, String owner, int requestsPerMinute) {
        String rawKey = generateRawKey();
        String keyHash = hashKey(rawKey);

        ApiKey apiKey = ApiKey.builder()
                .keyHash(keyHash)
                .name(name)
                .owner(owner)
                .active(true)
                .requestsPerMinute(requestsPerMinute > 0 ? requestsPerMinute : 60)
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);
        return new ApiKeyCreationResult(rawKey, saved);
    }

    public Optional<ApiKey> validateKey(String rawKey) {
        String keyHash = hashKey(rawKey);
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyHash(keyHash);

        if (apiKeyOpt.isPresent()) {
            ApiKey apiKey = apiKeyOpt.get();
            if (!apiKey.isActive()) {
                return Optional.empty();
            }
            if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(Instant.now())) {
                return Optional.empty();
            }
            apiKey.setLastUsedAt(Instant.now());
            apiKeyRepository.save(apiKey);
            return Optional.of(apiKey);
        }

        return Optional.empty();
    }

    public void deactivateKey(Long id) {
        apiKeyRepository.findById(id).ifPresent(key -> {
            key.setActive(false);
            apiKeyRepository.save(key);
        });
    }

    private String generateRawKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "llmgw_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
